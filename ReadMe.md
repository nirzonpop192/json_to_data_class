# 📦 Kotlin JSON → DTO + Domain + Mapper Generator

A lightweight Kotlin JVM tool that automatically generates DTO models, Domain models, and Mapper extensions from a JSON response, following Clean Architecture best practices.

## 🚀 Features

✅ Generates DTO classes (nullable, API-safe)

✅ Generates Domain models (non-nullable, business-safe)

✅ Generates Mapper functions (Dto → Domain)

✅ Supports nested objects

✅ Supports lists of objects

✅ Converts snake_case → camelCase

✅ Uses Gson annotations

✅ Interactive popup dialog for root class name

✅ Writes output to a structured output/ directory

## 🧱 Generated Architecture

The tool generates files following this structure:

output/
```
├── dto/
│   ├── ApiResponseDto.kt
│   ├── TopicDto.kt
│   └── PaginationDto.kt
│
├── domain/
│   ├── ApiResponse.kt
│   ├── Topic.kt
│   └── Pagination.kt
│
└── mapper/
├── ApiResponseMapper.kt
├── TopicMapper.kt
└── PaginationMapper.kt

```
This structure is Clean Architecture compliant and ready to be copied into an Android project.

##🧠 Design Philosophy
DTO Layer

Matches API exactly

Nullable properties

Uses @SerializedName

Safe against backend changes

Domain Layer

Non-nullable

No framework annotations

Safe for business logic and UI

Mapper Layer

Handles nullability

Applies default values

Converts DTO → Domain cleanly

🛠️ Tech Stack

Language: Kotlin (JVM)

JSON Parsing: org.json

Serialization: Gson annotations

UI: Swing (JOptionPane)

Architecture: Clean Architecture principles

📋 Requirements

JDK 8 or higher

Kotlin JVM

Desktop environment (for popup dialog)

▶️ How to Run

Clone the repository

Open the project in IntelliJ IDEA (or any Kotlin-compatible IDE)

Paste your API JSON into jsonString

Run main()

A popup will appear asking for the Root Class Name.

Example:

ApiResponse

🧪 Example Mapping Logic
fun TopicDto.toDomain(): Topic = Topic(
titleEn = titleEn.orEmpty(),
commentCount = commentCount ?: 0,
isVisible = isVisible ?: false
)


This ensures:

No null leaks into domain/UI

Safe defaults are applied centrally

📂 Output Location

All generated files are written to:

./output/


The folder is created automatically if it does not exist.

🔐 Why Nullable Is Not Removed from DTOs

APIs may:

Return null

Change contracts

Omit fields

Keeping DTOs nullable prevents:
❌ Runtime crashes
❌ Gson parsing errors

Null handling is intentionally done only in the mapper layer.

🧩 Extensibility

This tool can be easily extended to support:

📦 Custom package names

📂 Output directory picker

🧪 Auto-generated unit tests

🔁 Moshi instead of Gson

🛠 CLI / Gradle task

📱 Flutter (Dart) model generation

⚠️ Limitations

Uses the first object of a JSON array to infer structure

Assumes consistent array element schema

Does not validate JSON schema

🤝 Contribution

Contributions and improvements are welcome.

Suggested areas:

Better type inference

Sealed class support

Enum detection

Kotlinx Serialization support

📄 License

This project is intended for educational and internal tooling purposes.
You may adapt and reuse it freely within your projects.

🙌 Author

Faisal Mohammad
Android / Software Engineer
Focused on Clean Architecture & scalable systems
