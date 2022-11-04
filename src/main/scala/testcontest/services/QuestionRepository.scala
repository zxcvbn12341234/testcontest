package testcontest.services

import cats._
import cats.effect.std.Console
import cats.effect.{Ref, Sync}
import cats.syntax.all._
import testcontest.model._
import testcontest.model.Question.JsonSupport._

import java.util.UUID

trait QuestionRepository[F[_]] {
  def getQuestion(questionId: UUID): F[Option[Question]]
  def getAllQuestions: F[List[Question]]
  def putQuestion(question: Question): F[Question]
  def deleteQuestion(questionId: UUID): F[Unit]

  def updateAnswers(questionId: UUID, answers: List[String]): F[Unit]
  def updateCorrectAnswer(questionId: UUID, correctAnswer: Short): F[Unit]

  def start: F[Unit]
  def finished: F[Unit]
}

object QuestionRepository {

  def makeInMemory[F[_]: Sync: Console](implicit
      ev: ApplicativeError[F, Throwable]
  ): F[QuestionRepository[F]] =
    Ref.of[F, Map[UUID, Question]](Map.empty[UUID, Question]).map {
      questionDatabaseF =>
        new QuestionRepository[F] with BaseRepository[F, UUID, Question] {

          override val storageRef: Ref[F, Map[UUID, Question]] =
            questionDatabaseF

          override val repositoryName = "question-repository"

          override def key(value: Question): UUID = value.id

          override def getQuestion(questionId: UUID): F[Option[Question]] =
            get(questionId)

          override def getAllQuestions: F[List[Question]] = getAll

          override def putQuestion(question: Question): F[Question] =
            put(question)

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

          override def start: F[Unit] =
            Console[F].println("Initializing question repository from file...") >>
              readSnapshot >>
              Console[F].println("Initialized.")

          override def finished: F[Unit] =
            Console[F].println("Saving question repository file...") >>
              saveSnapshot >>
              Console[F].println("Saved.")
        }
    }
}
