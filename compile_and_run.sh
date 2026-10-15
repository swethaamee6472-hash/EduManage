# EduManage - Student Data Management System
# Run with: .\compile_and_run.sh  (PowerShell on Windows)

$BASE_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$SRC_DIR  = "$BASE_DIR\src"
$BIN_DIR  = "$BASE_DIR\bin"
$DATA_DIR = "$BASE_DIR\data"

if (!(Test-Path $BIN_DIR))  { New-Item -ItemType Directory -Path $BIN_DIR  | Out-Null }
if (!(Test-Path $DATA_DIR)) { New-Item -ItemType Directory -Path $DATA_DIR | Out-Null }

Write-Host "=========================================================="
Write-Host " EduManage - Student Data Management System"
Write-Host " Module 1: Authentication & Role Management (Swing)"
Write-Host "=========================================================="
Write-Host "Compiling Java source files..."

# Build lib classpath
$libJars = (Get-ChildItem "$BASE_DIR\lib\*.jar" | ForEach-Object { $_.FullName }) -join ";"

# Collect all .java source files
$sources = Get-ChildItem -Recurse -Path $SRC_DIR -Filter "*.java" | ForEach-Object { "`"$($_.FullName)`"" }
$sourcesFile = "$BASE_DIR\sources.txt"
$sources | Out-File -Encoding ascii -FilePath $sourcesFile

# Compile
& javac -cp $libJars -d $BIN_DIR "@$sourcesFile"
Remove-Item $sourcesFile -ErrorAction SilentlyContinue

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Compilation completed successfully into bin\ directory." -ForegroundColor Green

if ($args[0] -eq "--test") {
    Write-Host ""
    Write-Host "Running automated verification test suite..."
    & java -cp "$BIN_DIR;$libJars" com.sset.erp.test.TestRunner
} else {
    Write-Host "Launching EduManage ERP GUI..."
    Write-Host ""
    Write-Host "Demo Credentials for Testing:"
    Write-Host "  • Administrator : admin / admin123"
    Write-Host "  • Faculty       : faculty_cs / faculty123"
    Write-Host "  • Student       : student_cs / student123"
    Write-Host "  • Parent        : parent_cs / parent123"
    Write-Host "=========================================================="
    & java -cp "$BIN_DIR;$libJars" com.sset.erp.Main
}
