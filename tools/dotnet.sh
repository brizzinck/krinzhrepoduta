#!/bin/sh
# .NET SDK 10 in docker. The NuGet cache lives in the docker volume duikt-nuget.
# Example: ../../tools/dotnet.sh build -c Release
docker volume inspect duikt-nuget >/dev/null 2>&1 || {
  docker volume create duikt-nuget >/dev/null
  docker run --rm -v duikt-nuget:/nuget alpine:3 chown "$(id -u):$(id -g)" /nuget
}
exec docker run --rm -u "$(id -u):$(id -g)" \
  -e HOME=/tmp -e DOTNET_CLI_HOME=/tmp -e NUGET_PACKAGES=/nuget \
  -e DOTNET_NOLOGO=1 -e DOTNET_CLI_TELEMETRY_OPTOUT=1 -e DOTNET_SKIP_FIRST_TIME_EXPERIENCE=1 \
  -v duikt-nuget:/nuget -v "$PWD":/w -w /w \
  mcr.microsoft.com/dotnet/sdk:10.0 dotnet "$@"
