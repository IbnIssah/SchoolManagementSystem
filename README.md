# School Management System (Java Swing)

This is a desktop School Management System written in Java using Swing for the UI and SQLite for storage. It provides basic school administration features such as managing admins, students, teachers, subjects, attendance, and payments.

## Key Features

- Admin login/logout with persistent session (optionally restored on restart)
- First-run flow: the app prompts to create the first admin if no admin accounts exist
- Dashboard showing total students, teachers and fees collected (hidden/zeroed until admin logs in)
- Student and teacher management (add, edit, delete, import/export CSV)
- Simple backup/restore for the database
- Role-controlled UI: most actions and menus are disabled until an admin logs in

## Repository layout

- `src/` - Java source files
	- `school.management.system` - main application classes and entry point
	- `school.management.system.ui` - Swing UI panels and dialogs
	- `school.management.system.ui.adm` - admin-related dialogs (AddAdmin, AdminLog)
	- `school.management.system.data` - database access layer (`DB.java`, `DataSource`)
	- `school.management.system.util` - utility classes (login helpers, helpers, etc.)
- `libs/` - external libraries and the SQLite DB file (`libs/db/main.db`)
- `bin/` - compiled classes (if present)
- `img/` - images used by UI

## Requirements

- Java 17+ (project has been run with GraalVM JDK 25 in the author's environment but any Java 17+ runtime should work)
- SQLite (the project uses an embedded SQLite DB file; no external server required)

## How to build & run (Windows PowerShell)

1. Open PowerShell and change directory to the project root (where this README is located):

```powershell
cd 'path/to/your/project/School Management System'
```

2. Compile and run using your Java installation. Example (the project has been run with GraalVM in the original environment):

```powershell
# Compile (if you need to)
javac -d bin -sourcepath src (Get-ChildItem -Recurse -Filter "*.java" -Path src).FullName

# Run (example, the project's Main class):
java -cp "bin;libs/*" school.management.system.Main
```

Notes:
- In practice the workspace already includes a compiled `bin/` folder in some setups. The easiest run command is the one used by your environment (for example, the VS Code Run task or the IDE run target that invokes `school.management.system.Main`).

## First run behavior

When the application runs for the first time and no admin exists in the database, it will automatically prompt you to create the first admin via the `Add Admin` dialog. After creating the first admin, the app auto-logs that admin in and enables admin-only menus and controls.

## Persistence and Auto-login

- The app persists a small session flag in Java Preferences to remember if an admin was logged in. It also persists the admin's username and will try to restore that admin on restart so the UI shows the correct greeting and enabled features.
- If an admin manually logs out, the persisted session information is cleared.

## Troubleshooting

- If you see a NullPointerException related to menus at startup, ensure the `App` class initializes the menu bar before panels (this project already guards that initialization order).
- If the dashboard shows zeros, ensure an admin is logged in — the dashboard intentionally displays zeros until an admin logs in for security.
- Database file is located at `libs/db/main.db`. If you need to reset data during testing, stop the app and remove or replace that file (be careful — this deletes data). Ignore those pop-up error messages when restarting the app without the `main.db` file. A new one is automatically created. 

## Contributing

If you'd like to contribute, please:

1. Create a feature branch from `main`.
2. Make small, focused commits.
3. Run the application and add/update tests if you add new logic.

## Quick dev notes

- The `AuthService` class contains login/logout helpers. `AuthService.attemptAutoLogin()` restores a logged-in admin by reading `isAdminLoggedIn` and a saved `lastAdminUser` preference, then populating the in-memory `Admin` object from the DB.
- The `MainPanel` contains the greeting label and guards to keep admin-only UI disabled until `AuthService.setLoggedIn(true)` is called.

