# Trigger Jenkins Build and Monitor
$ErrorActionPreference = "Stop"
$jenkinsUrl = "http://localhost:8081"
$jobName = "visitor-gate-pass-pipeline"

Write-Host "Obtaining Jenkins CSRF Crumb..."
$crumbInfo = Invoke-RestMethod -Uri "$jenkinsUrl/crumbIssuer/api/json" -Method Get -SessionVariable session
$headers = @{}
$headers.Add($crumbInfo.crumbRequestField, $crumbInfo.crumb)

Write-Host "Triggering pipeline build..."
$triggerUrl = "$jenkinsUrl/job/$jobName/buildWithParameters?DEPLOY_ENV=local&APP_PORT=8080&RUN_UI_TESTS=false"
Invoke-RestMethod -Uri $triggerUrl -Method Post -WebSession $session -Headers $headers

Write-Host "Build queued successfully. Waiting for build execution..."
Start-Sleep -Seconds 8

$jobInfo = Invoke-RestMethod -Uri "$jenkinsUrl/job/$jobName/api/json" -Method Get -WebSession $session
$buildNum = $jobInfo.lastBuild.number
Write-Host "Tracking Build #$buildNum..."

do {
    Start-Sleep -Seconds 5
    $buildInfo = Invoke-RestMethod -Uri "$jenkinsUrl/job/$jobName/$buildNum/api/json" -Method Get -WebSession $session
    $building = $buildInfo.building
    $result = $buildInfo.result
    Write-Host "Build #$buildNum status: building=$building, result=$result"
} while ($building)

Write-Host "Build #$buildNum finished with result: $result"
if ($result -ne "SUCCESS") {
    exit 1
}
