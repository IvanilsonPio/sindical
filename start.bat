@echo off
REM Sistema Sindicato Rural - Quick Start Script for Windows

echo ==========================================
echo Sistema Sindicato Rural - Inicializacao
echo ==========================================
echo.

REM Check if Docker is installed
docker --version >nul 2>&1
if errorlevel 1 (
    echo X Docker nao esta instalado. Por favor, instale o Docker primeiro.
    exit /b 1
)

REM Check if Docker Compose is installed
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo X Docker Compose nao esta instalado. Por favor, instale o Docker Compose primeiro.
    exit /b 1
)

REM Check if .env file exists
if not exist .env (
    echo ! Arquivo .env nao encontrado. Criando a partir de .env.example...
    copy .env.example .env
    echo OK Arquivo .env criado. Por favor, revise as configuracoes antes de continuar.
    echo.
    echo IMPORTANTE: Altere o JWT_SECRET no arquivo .env para producao!
    echo.
    pause
)

REM Ask which environment to start
echo Escolha o ambiente:
echo 1) Desenvolvimento (apenas PostgreSQL)
echo 2) Producao (todos os servicos)
set /p choice="Digite sua escolha (1 ou 2): "

if "%choice%"=="1" (
    echo.
    echo Iniciando ambiente de desenvolvimento...
    docker-compose -f docker-compose.dev.yml up -d
    echo.
    echo OK PostgreSQL iniciado!
    echo.
    echo Para iniciar o backend:
    echo   mvn spring-boot:run
    echo.
    echo Para iniciar o frontend:
    echo   cd frontend ^&^& npm install ^&^& npm start
    echo.
    echo Acesso:
    echo   Frontend: http://localhost:4200
    echo   Backend: http://localhost:8080
    echo   PostgreSQL: localhost:5432
) else if "%choice%"=="2" (
    echo.
    echo Iniciando ambiente de producao...
    docker-compose up -d --build
    echo.
    echo Aguardando servicos iniciarem...
    timeout /t 10 /nobreak >nul
    echo.
    echo OK Aplicacao iniciada!
    echo.
    echo Acesso:
    echo   Frontend: http://localhost
    echo   Backend: http://localhost:8080
    echo.
    echo Para ver os logs:
    echo   docker-compose logs -f
    echo.
    echo Para parar os servicos:
    echo   docker-compose down
) else (
    echo X Opcao invalida
    exit /b 1
)

echo.
echo ==========================================
echo Inicializacao concluida!
echo ==========================================
pause
