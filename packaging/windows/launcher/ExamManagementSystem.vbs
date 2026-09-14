' ==============================================================================
' Exam Management System - Silent VBScript Launcher (Windows)
' Launches ExamManagementSystem.bat completely hidden (no console window flash).
' ==============================================================================

Set objFSO = CreateObject("Scripting.FileSystemObject")
Set objShell = CreateObject("WScript.Shell")

strScriptDir = objFSO.GetParentFolderName(WScript.ScriptFullName)
strBatPath = objFSO.BuildPath(strScriptDir, "ExamManagementSystem.bat")

If Not objFSO.FileExists(strBatPath) Then
    strBatPath = objFSO.BuildPath(objFSO.BuildPath(strScriptDir, "launcher"), "ExamManagementSystem.bat")
End If

If objFSO.FileExists(strBatPath) Then
    ' 0 = Hide window, False = Do not wait for script termination
    objShell.Run Chr(34) & strBatPath & Chr(34), 0, False
Else
    MsgBox "Could not locate ExamManagementSystem.bat in application directory.", 16, "Exam Management System"
End If
