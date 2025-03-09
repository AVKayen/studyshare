# StudyShare

StudyShare is a web application for users to share their tasks to be solved by others.

## Features
- Create groups
- Add and remove members from your group
- Create tasks
- Share solutions
- Rate and comment on posts
- Edit and delete your posts
- Attach images

## Technologies
- Kotlin
- Ktor
- HTML DSL
- HTMX
- Hyperscript
- MongoDB

## Configuration
Due to the structure of Google App Engine, the app **requires you** to write your own implementation of the 
`com.studyshare.environment.Environment` class, which must expose the necessary environment variables,
MongoDB connection string and Google Cloud Storage bucket names.

## How to run
1. Clone the repository
2. Run `./gradlew run` in the root directory
3. The server will be running on `localhost:8080`