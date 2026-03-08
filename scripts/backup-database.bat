@echo off
REM Script de backup automático do PostgreSQL para Windows
REM Este script cria backups diários do banco de dados do sistema

REM Configurações
set BACKUP_DIR=C:\backups\sindicato-rural
set DB_NAME=sindicato_rural
set DB_USER=postgres
set DB_HOST=localhost
set DB_PORT=5432
set RETENTION_DAYS=30

REM Criar diretório de backup se não existir
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

REM Nome do arquivo de backup com timestamp
for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set DATE=%%c%%a%%b)
for /f "tokens=1-2 delims=: " %%a in ('time /t') do (set TIME=%%a%%b)
set TIMESTAMP=%DATE%_%TIME%
set BACKUP_FILE=%BACKUP_DIR%\backup_%DB_NAME%_%TIMESTAMP%.sql

REM Log
echo [%date% %time%] Iniciando backup do banco de dados %DB_NAME%...

REM Executar backup usando pg_dump
"C:\Program Files\PostgreSQL\15\bin\pg_dump.exe" ^
    -h %DB_HOST% ^
    -p %DB_PORT% ^
    -U %DB_USER% ^
    -d %DB_NAME% ^
    --format=plain ^
    --no-owner ^
    --no-acl ^
    --verbose ^
    -f "%BACKUP_FILE%"

REM Verificar se o backup foi bem-sucedido
if %ERRORLEVEL% EQU 0 (
    echo [%date% %time%] Backup criado com sucesso: %BACKUP_FILE%
    
    REM Comprimir o backup
    echo [%date% %time%] Comprimindo backup...
    powershell Compress-Archive -Path "%BACKUP_FILE%" -DestinationPath "%BACKUP_FILE%.zip"
    del "%BACKUP_FILE%"
    
    REM Remover backups antigos (mais de RETENTION_DAYS dias)
    echo [%date% %time%] Removendo backups com mais de %RETENTION_DAYS% dias...
    forfiles /P "%BACKUP_DIR%" /M backup_%DB_NAME%_*.zip /D -%RETENTION_DAYS% /C "cmd /c del @path" 2>nul
    
    echo [%date% %time%] Backup concluído com sucesso!
    exit /b 0
) else (
    echo [%date% %time%] ERRO: Falha ao criar backup!
    exit /b 1
)
