package testcontest.services

import testcontest.model.Question

import cats._
import cats.data._
import cats.implicits._

import java.util.UUID

trait QuestionService[F[_]] {
  def addQuestion(
      description: String,
      answers: List[String],
      correctAnswer: Short
  ): F[Question]
  def getQuestion(questionId: UUID): F[Option[Question]]
  def deleteQuestion(questionId: UUID): F[Unit]
  def allQuestions: F[List[Question]]
}

object QuestionService {

  def make[F[_]](
      questionRepository: QuestionRepository[F]
  )(implicit ev: MonadError[F, Throwable]): QuestionService[F] =

    new QuestionService[F] {
      override def addQuestion(
          description: String,
          answers: List[String],
          correctAnswer: Short
      ): F[Question] = {
        val question = Question.question(description, answers, correctAnswer)
        questionRepository.getQuestion(question.id).flatMap {
          // id collision, generate again
          case Some(_) => addQuestion(description, answers, correctAnswer)
          case None    => questionRepository.putQuestion(question)
        }
      }

      override def getQuestion(questionId: UUID): F[Option[Question]] =
        questionRepository.getQuestion(questionId)

      override def deleteQuestion(questionId: UUID): F[Unit] =
        questionRepository.deleteQuestion(questionId)

      override def allQuestions: F[List[Question]] =
        questionRepository.getAllQuestions
    }
}
