


interface Message {
    role: "user" | "system" | "assistant" | "tool",
    content: string,
    isToolCall: boolean
}

type status = "message" | "response" | "responseEnd"

interface Response {
    status_: status,
    content: Message[] | string
}