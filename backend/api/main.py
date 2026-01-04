import os
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from openai import OpenAI

from dotenv import load_dotenv

load_dotenv()

app = FastAPI()


app.add_middleware(
    CORSMiddleware,

    allow_origins=["http://localhost:3000"], 
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# OpenAIクライアント設定

client = OpenAI(
    api_key=os.environ.get("OPENAI_API_KEY"),
)

class ChatRequest(BaseModel):
    message: str

@app.post("/chat")
def chat(request: ChatRequest):
    try:
        # AI早苗としての応答生成
        response = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": "あなたは高市早苗、日本の内閣総理大臣です。「〜だね」「〜ですよ」といった口調で、国民の質問に答えてください。"},
                {"role": "user", "content": request.message}
            ]
        )
        
        ai_message = response.choices[0].message.content
        return {"reply": ai_message}

    except Exception as e:
        print(f"Error: {e}")
        return {"error": str(e)}