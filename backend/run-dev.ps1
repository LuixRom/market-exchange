# Carga backend/.env en variables de entorno de esta sesion y levanta el backend.
# Uso: desde backend/  ->  .\run-dev.ps1

$envFile = Join-Path $PSScriptRoot ".env"
if (-not (Test-Path $envFile)) {
    Write-Error "No se encontro $envFile. Copia .env.example a .env y completa los valores."
    exit 1
}

Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq "" -or $line.StartsWith("#")) { return }

    $idx = $line.IndexOf("=")
    if ($idx -lt 1) { return }

    $name = $line.Substring(0, $idx).Trim()
    $value = $line.Substring($idx + 1).Trim()
    Set-Item -Path "env:$name" -Value $value
}

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "Variables cargadas desde $envFile." -ForegroundColor Green

# 'clean' evita UnsupportedClassVersionError si algo (p. ej. IntelliJ con otro JDK
# configurado) recompilo target/classes con una version de Java distinta a la 17
# que usa este script.
& "$PSScriptRoot\mvnw.cmd" clean

Write-Host "Levantando backend (perfil: $env:SPRING_PROFILES_ACTIVE)..." -ForegroundColor Green
& "$PSScriptRoot\mvnw.cmd" spring-boot:run
