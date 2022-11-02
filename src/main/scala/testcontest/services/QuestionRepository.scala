package testcontest.services

import cats._
import cats.effect.{Ref, Sync}
import cats.syntax.all._
import testcontest.model._

import java.util.UUID

trait QuestionRepository[F[_]] {
  def getQuestion(questionId: UUID): F[Option[Question]]
  def getAllQuestions: F[List[Question]]
  def putQuestion(question: Question): F[Question]
  def deleteQuestion(questionId: UUID): F[Unit]

  def updateAnswers(questionId: UUID, answers: List[String]): F[Unit]
  def updateCorrectAnswer(questionId: UUID, correctAnswer: Short): F[Unit]
}

object QuestionRepository {

  def makeInMemory[F[_]: Sync](implicit
      ev: ApplicativeError[F, Throwable]
  ): F[QuestionRepository[F]] =
    Ref.of[F, Map[UUID, Question]](Map.empty[UUID, Question]).map {
      questionDatabaseF =>
        new QuestionRepository[F] with BaseRepository[F, Question] {

          override val storageRef: Ref[F, Map[UUID, Question]] =
            questionDatabaseF
          override implicit val ev: Applicative[F] = implicitly[Applicative[F]]

          override def getQuestion(questionId: UUID): F[Option[Question]] =
            get(questionId)

          override def getAllQuestions: F[List[Question]] = getAll

          override def putQuestion(question: Question): F[Question] =
            put(question.id, question)

          override def deleteQuestion(questionId: UUID): F[Unit] =
            delete(questionId)

          override def updateAnswers(
              questionId: UUID,
              answers: List[String]
          ): F[Unit] = update(
            questionId,
            question => question.copy(answers = answers)
          )

          override def updateCorrectAnswer(
              questionId: UUID,
              correctAnswer: Short
          ): F[Unit] = update(
            questionId,
            question => question.copy(correctAnswer = correctAnswer)
          )
        }
    }
}
