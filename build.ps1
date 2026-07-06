# ============================================================
#  build.ps1 - compila o plugin SeasonRPG Demo sem Gradle.
#  Baixa o Paper 1.21.4 (usado como classpath e como servidor),
#  compila as fontes com javac e empacota o .jar em build/.
# ============================================================
$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

# Versao do Paper. Troque aqui se quiser outra 1.21.x.
$paperVersion = "1.21.4"
$paperJar     = Join-Path $root "server\paper-$paperVersion.jar"

# --- localizar o JDK 21 ---------------------------------------------------
function Find-Java {
    if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
        return $env:JAVA_HOME
    }
    $candidate = Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -like "jdk-21*" } | Select-Object -First 1
    if ($candidate) { return $candidate.FullName }
    throw "JDK 21 nao encontrado. Instale o Temurin 21 e/ou defina JAVA_HOME."
}
$javaHome = Find-Java
$javac = Join-Path $javaHome "bin\javac.exe"
$jarTool = Join-Path $javaHome "bin\jar.exe"
Write-Host "Usando JDK: $javaHome"

# --- baixar o Paper -------------------------------------------------------
if (-not (Test-Path $paperJar)) {
    New-Item -ItemType Directory -Force (Join-Path $root "server") | Out-Null
    Write-Host "Consultando ultimo build do Paper $paperVersion (API v3)..."
    $headers = @{ "User-Agent" = "SeasonRPGDemo/1.0" }
    $latest = Invoke-RestMethod "https://fill.papermc.io/v3/projects/paper/versions/$paperVersion/builds/latest" -Headers $headers
    $url = $latest.downloads.'server:default'.url
    Write-Host "Baixando Paper $paperVersion build $($latest.id)..."
    # curl.exe lida melhor com arquivos grandes que Invoke-WebRequest.
    & curl.exe -L --retry 3 -A "SeasonRPGDemo/1.0" -o $paperJar $url
}

# O jar do Paper e um "paperclip": as classes da API so aparecem apos o
# patch. Extraimos uma vez (patchonly) para obter a paper-api e as libs.
$apiJar = Join-Path $root "server\libraries\io\papermc\paper\paper-api\$paperVersion-R0.1-SNAPSHOT\paper-api-$paperVersion-R0.1-SNAPSHOT.jar"
if (-not (Test-Path $apiJar)) {
    Write-Host "Extraindo a API do Paper (patchonly)..."
    Push-Location (Join-Path $root "server")
    & (Join-Path $javaHome "bin\java.exe") "-Dpaperclip.patchonly=true" -jar $paperJar | Out-Null
    Pop-Location
}
# Classpath de compilacao: paper-api + todas as libs extraidas (Adventure etc.).
$libJars = Get-ChildItem -Recurse -Filter *.jar (Join-Path $root "server\libraries") |
    ForEach-Object { $_.FullName }
$classpath = ($libJars -join ';')

# --- compilar -------------------------------------------------------------
$srcDir = Join-Path $root "src\main\java"
$outDir = Join-Path $root "build\classes"
$buildDir = Join-Path $root "build"
if (Test-Path $outDir) { Remove-Item -Recurse -Force $outDir }
New-Item -ItemType Directory -Force $outDir | Out-Null

$sources = Get-ChildItem -Recurse -Filter *.java $srcDir | ForEach-Object { $_.FullName }
Write-Host "Compilando $($sources.Count) arquivos..."
& $javac -encoding UTF-8 -proc:none -cp $classpath -d $outDir @sources
if ($LASTEXITCODE -ne 0) { throw "Falha na compilacao." }

# --- copiar recursos e empacotar -----------------------------------------
Copy-Item -Recurse -Force (Join-Path $root "src\main\resources\*") $outDir
$finalJar = Join-Path $buildDir "SeasonRPGDemo-0.1.0.jar"
if (Test-Path $finalJar) { Remove-Item -Force $finalJar }
& $jarTool -c -f $finalJar -C $outDir .
if ($LASTEXITCODE -ne 0) { throw "Falha ao empacotar." }

Write-Host ""
Write-Host "OK -> $finalJar" -ForegroundColor Green
Write-Host "Copie esse .jar para server\plugins\ e inicie o servidor."
