# Code Review Tool

A tool that uses Google's Gemini AI to perform automated code reviews.

## Setup

1. Copy the environment template to create your `.env` file:
   ```bash
   cp .env.template .env
   ```

2. Edit the `.env` file and add your Gemini API key:
   ```
   GEMINI_API_KEY=your_actual_api_key_here
   ```

3. Build the project:
   ```bash
   ./gradlew build
   ```

4. Run the application:
   ```bash
   ./gradlew run
   ```

## Usage

To review a file, make a GET request to:
```
http://localhost:8080/api/review/{fileName}
```

Replace `{fileName}` with the name of the file you want to review.

The service will:
1. Read the specified file
2. Send it to Gemini AI for review
3. Create a new file with suggested changes (prefixed with "reviewed_")
4. Return a JSON response with detailed suggestions

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| GEMINI_API_KEY | Your Google Gemini API key | Yes |

You can set environment variables either in the `.env` file or as system environment variables. The system environment variables take precedence over the `.env` file. 