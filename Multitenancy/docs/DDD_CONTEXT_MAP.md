# Domain and Context Map

## Domains
- Identity and Access
- Projects and Issues
- Workflow
- Comments and Audit
- Attachments and Storage
- Reporting
- Notifications and Webhooks

## Relationships
- Issues depend on Projects
- Workflow drives Issue state
- Comments and Audit record actions on Issues and Projects
- Attachments link to Issues
- Reporting builds read side views
- Notifications and Webhooks consume Audit events

## Extraction seams
- Webhook delivery
- Reporting read side
- Search service
- Notification sender

## Anti corruption strategy
- External policy decisions via policy client
- External identity uses OIDC tokens with claim mapping
