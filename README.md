# Code Review Tool

A Kotlin-based web service that leverages Google's Gemini AI to perform automated code reviews. The tool analyzes your code files and provides detailed suggestions for improvements, best practices, and potential issues.

## Features

- Automated code review using Gemini AI
- RESTful API endpoints for code review requests
- Creates reviewed versions of files with suggested improvements
- JSON-formatted review responses
- Built with Ktor framework for high performance
- Dependency injection using Koin
- Environment variable configuration support

## Prerequisites

- JDK 11 or higher
- Gradle build tool (wrapper included)
- Google Gemini API key

## Project Structure

```
.
├── src/
│   ├── main/
│   │   ├── kotlin/         # Kotlin source files
│   │   └── resources/      # Application resources
│   └── test/               # Test files
├── build.gradle.kts        # Gradle build configuration
├── .env.template          # Environment variables template
└── README.md             # This file
```

## Setup

1. Clone the repository:
   ```bash
   git clone git@github.com:Anubhavk-surya/code-review-tool.git
   cd code-review-tool
   ```

2. Copy the environment template to create your `.env` file:
   ```bash
   cp .env.template .env
   ```

3. Edit the `.env` file and add your Gemini API key:
   ```
   GEMINI_API_KEY=your_actual_api_key_here
   ```
4. Note: If you don’t have an API Key, create one by visiting [this site](https://aistudio.google.com/app/apikey)

5. Build the project:
   ```bash
   ./gradlew build
   ```
   
6. Run the application:
   ```bash
   ./gradlew run
   ```

The server will start on port 8080 by default. You can customize the port by setting the `PORT` environment variable.

## API Usage

### Code Review Endpoint

**Request:**
```http
POST http://localhost:8080/api/review
```

**Request Body:**
```json
{
  "fileName": "/path/to/your/file.kt",
  "language": "kotlin",     
  "model": "gemini-2.0-flash"  
}
```

**Example using curl:**
```bash
# Review a file using absolute path
curl -X POST http://localhost:8080/api/review \
  -H "Content-Type: application/json" \
  -d '{"fileName": "/home/user/projects/MyFile.kt"}'

# Review a file using relative path (relative to where you run the curl command)
curl -X POST http://localhost:8080/api/review \
  -H "Content-Type: application/json" \
  -d '{"fileName": "./src/main/kotlin/MyFile.kt"}'

# Review with custom language and model
curl -X POST http://localhost:8080/api/review \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "./src/main/kotlin/MyFile.kt",
    "language": "kotlin",
    "model": "gemini-2.0-flash"
  }'
```

**Response:**
```json
{
  "suggestions": "Detailed review comments and suggestions",
  "reviewedFilePath": "Path to the reviewed file"
}
```

The service will:
1. Read the specified file
2. Send it to Gemini AI for review
3. Create a new file with suggested changes (prefixed with "reviewed_")
4. Return a JSON response with detailed suggestions

## Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| GEMINI_API_KEY | Your Google Gemini API key | Yes | - |
| PORT | Server port number | No | 8080 |

Environment variables can be set either in the `.env` file or as system environment variables. System environment variables take precedence over the `.env` file.

## Development

The project uses the following main dependencies:
- Ktor: Web framework
- Koin: Dependency injection
- Kotlin Serialization: JSON handling
- Google Gemini AI SDK: Code analysis
