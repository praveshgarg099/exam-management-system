; ==============================================================================
; Exam Management System - Inno Setup 6 Installer Script
; Produces a standalone, single-file Windows installer: ExamManagementSystem-Setup.exe
; ==============================================================================

#define MyAppName "Exam Management System"
#define MyAppVersion "2.0.6"
#define MyAppPublisher "Exam Management System Team"
#define MyAppURL "https://github.com/praveshgarg099/exam-management-system"
#define MyAppBatName "ExamManagementSystem.bat"

[Setup]
; Unique AppId generated specifically for EMS upgrade detection
AppId={{C82390B1-5D34-4A1C-9E4B-871B045890AE}}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={autopf}\{#MyAppName}
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
OutputDir=..\..\..\dist\installer
OutputBaseFilename=ExamManagementSystem-Setup
Compression=lzma2/max
SolidCompression=yes
WizardStyle=modern
ArchitecturesInstallIn64BitMode=x64compatible
PrivilegesRequiredOverridesAllowed=commandline dialog
PrivilegesRequired=lowest
UninstallDisplayIcon={app}\{#MyAppBatName}
SetupLogging=yes

VersionInfoVersion=2.0.6.0
VersionInfoCompany=Exam Management System Team
VersionInfoDescription=Exam Management System Desktop Application
VersionInfoTextVersion=2.0.6
VersionInfoCopyright=Copyright (C) 2026 Exam Management System Team
VersionInfoProductName=Exam Management System
VersionInfoProductVersion=2.0.6.0

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
; Primary application executable and configuration
Source: "..\..\..\dist\exam-management-system.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\..\..\dist\lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\..\..\dist\conf\*"; DestDir: "{app}\conf"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\launcher\ExamManagementSystem.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\scripts\*"; DestDir: "{app}\scripts"; Flags: ignoreversion recursesubdirs createallsubdirs

; Custom modular Java Runtime Environment (~50MB jlink image)
Source: "..\..\..\target\runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs skipifsourcedoesntexist

; Bundled portable PostgreSQL binaries (if staged during build)
Source: "..\..\..\dist\pgsql\*"; DestDir: "{app}\pgsql"; Flags: ignoreversion recursesubdirs createallsubdirs skipifsourcedoesntexist

[Icons]
; Start Menu Shortcuts
Name: "{group}\{#MyAppName}"; Filename: "{app}\runtime\bin\javaw.exe"; Parameters: "-Dapp.home=""{app}"" -Dfile.encoding=UTF-8 -jar ""{app}\exam-management-system.jar"""; WorkingDir: "{app}"; Comment: "Launch Exam Management System"
Name: "{group}\{#MyAppName} (Debug Console)"; Filename: "{app}\{#MyAppBatName}"; Parameters: "--debug"; WorkingDir: "{app}"; Comment: "Launch Exam Management System with debug console"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"

; Desktop Shortcut
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\runtime\bin\javaw.exe"; Parameters: "-Dapp.home=""{app}"" -Dfile.encoding=UTF-8 -jar ""{app}\exam-management-system.jar"""; WorkingDir: "{app}"; Tasks: desktopicon; Comment: "Launch Exam Management System"

[Run]
; Option to launch the application immediately upon installation completion
Filename: "{app}\runtime\bin\javaw.exe"; Parameters: "-Dapp.home=""{app}"" -Dfile.encoding=UTF-8 -jar ""{app}\exam-management-system.jar"""; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: postinstall nowait skipifsilent

[UninstallRun]
; Gracefully shut down any running portable PostgreSQL cluster before file removal
Filename: "{app}\scripts\stop-local-postgres.bat"; Flags: runhidden waituntilterminated
