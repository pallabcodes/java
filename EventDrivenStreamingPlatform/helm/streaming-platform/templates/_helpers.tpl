{{/*
Expand the name of the chart.
*/}}
{{- define "streaming-platform.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "streaming-platform.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "streaming-platform.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "streaming-platform.labels" -}}
helm.sh/chart: {{ include "streaming-platform.chart" . }}
{{ include "streaming-platform.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "streaming-platform.selectorLabels" -}}
app.kubernetes.io/name: {{ include "streaming-platform.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "streaming-platform.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "streaming-platform.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Return the proper image name
*/}}
{{- define "streaming-platform.image" -}}
{{- $registry := .Values.global.imageRegistry | default "" }}
{{- if .image }}
{{- $image := .image }}
{{- if $registry }}
{{- printf "%s/%s:%s" $registry $image.repository ($image.tag | default .Chart.AppVersion) }}
{{- else }}
{{- printf "%s:%s" $image.repository ($image.tag | default .Chart.AppVersion) }}
{{- end }}
{{- end }}
{{- end }}
