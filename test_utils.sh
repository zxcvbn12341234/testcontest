
API_URL="localhost:8000/api"

function get_users() {
  http GET "$API_URL/user"
}

function get_all_questions() {
  http GET "$API_URL/question"
}

function add_question() {
  cat ./tests/add_question.json | http POST "$API_URL/question"
}

function add_user() {
  jq -n --arg username "$1" --arg password "$2" '{"username": $username, "password": $password}' | http POST "$API_URL/user"
}

function get_contest() {
  contestId=$1
  http GET "$API_URL/contest/$contestId"
}

function create_empty_contest() {
  http POST "$API_URL/contest"
}

function modify_dates_contest() {
  contestId=$1
  jq -n --arg startDate "2011-12-03T10:15:30+01:00" --arg endDate "2026-12-03T10:15:30+01:00" '{"startDate": $startDate, "endDate": $endDate}' | http POST "$API_URL/contest/$contestId/dates"
}

function add_participant() {
  contestId=$1
  jq -n --arg username $2 '{"users": [$username]}' | http POST "$API_URL/contest/$contestId/add-participants"
}

function add_question_to_contest() {
  contestId=$1
  jq -n --arg questionId $2 '{"questions":[$questionId]}' | http POST "$API_URL/contest/$contestId/add-questions"
}

function get_participant() {
  contestId=$1
  username=$2
  http GET "$API_URL/contest/$contestId/participant/$username"
}

function put_answers() {
  contestId=$1
  jq -n --arg username $2 --arg questionId $3 '{"username":$username,"answers":{($questionId):2}}' | http POST "$API_URL/contest/$contestId/put-answers"
}



