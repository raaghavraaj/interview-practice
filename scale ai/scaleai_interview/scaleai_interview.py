from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
import io
import csv
import json, requests
from concurrent.futures import ThreadPoolExecutor

server = FastAPI()

@server.get("/hello_world")
def hello_world() -> JSONResponse:
    return JSONResponse(content={"hello": "world"}, status_code=200)

@server.post("/ingest/tasks")
async def ingest_tasks(file: UploadFile = File(...)):
    content = await file.read()
    decoded_content = content.decode('utf-8')
    csv_file = io.StringIO(decoded_content)
    reader = csv.DictReader(csv_file)
    rows = list(reader)
    with ThreadPoolExecutor(max_workers=10) as executor:
        data = list(executor.map(classify_and_augment_task, rows))
    with open("tasks.json", "w") as json_file:
        json.dump(data, json_file, indent=2)
    return JSONResponse(content={"success": True}, status_code=200)

@server.post("/ingest/users")
async def ingest_users(file: UploadFile = File(...)):
    content = await file.read()
    decoded_content = content.decode('utf-8')
    csv_file = io.StringIO(decoded_content)
    reader = csv.DictReader(csv_file)
    rows = list(reader)
    with ThreadPoolExecutor(max_workers=10) as executor:
        data = list(executor.map(classify_and_augment_user, rows))
    with open("users.json", "w") as json_file:
        json.dump(data, json_file, indent=2)
    return JSONResponse(content={"success": True}, status_code=200)

URL = "https://litellm.ml.scaleinternal.com/v1/chat/completions"
API_KEY = "sk-mITm8IHM6p3CkY4tcU-f1w"
HEADERS = {
    "Content-Type": "application/json",
    "Authorization": f"Bearer {API_KEY}"
}
def classify_task_category(task: dict):
    data = {
        "model": "gpt-4o",
        "messages": [
            {
                "role": "user",
                "content": f"I will provide you with a task description and I want you to classify this task from one the following categories: Legal, Automotive, Geography, Healthcare, Math, Music. Here is the task description :{task['Task_String']}. Give me only the category. The one word response"
            }
        ]
    }
    response = requests.post(URL, headers=HEADERS, json=data)
    content = response.content
    content_dict = json.loads(content.decode("utf-8"))
    return content_dict["choices"][0]["message"]["content"]

def classify_and_augment_task(row):
    row["Category"] = classify_task_category(row)
    return row

def classify_user_specialization(user: dict):
    data = {
        "model": "gpt-4o",
        "messages": [
            {
                "role": "user",
                "content": f"I will provide you with a user's education description and its working experience and I want you to classify this user from one the following specializations: Mechanic/Technician, Healthcare Professional, Legal Professional, Software Developer, Other, Student. Here is the user description :{user['Education']} and here is the user experience: {user['Experience']}. Give me only the specialization. The one word response."
            }
        ]
    }
    response = requests.post(URL, headers=HEADERS, json=data)
    content = response.content
    content_dict = json.loads(content.decode("utf-8"))
    return content_dict["choices"][0]["message"]["content"]

def classify_and_augment_user(row):
    row["Specialization"] = classify_user_specialization(row)
    return row
