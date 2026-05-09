# Deploying to AKS

A working set of Kubernetes manifests for running the Spring Batch Dashboard on Azure Kubernetes Service (AKS) behind the Application Gateway Ingress Controller (AGIC). The manifests deploy a single Postgres-backed environment with one GitHub OAuth2 provider — adding more datasources or providers is documented under [Extending the example](#extending-the-example) below.

## What's in here

| File                                       | Purpose                                                                                                                            |
| ------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------- |
| [`namespace.yaml`](namespace.yaml)         | Creates `spring-batch-dashboard`.                                                                                                  |
| [`configmap.yaml`](configmap.yaml)         | Non-secret config — Spring profile, OAuth2 client id + button styling, datasource URL/username/timezone, OAuth2 success URL.       |
| [`secret.yaml`](secret.yaml)               | OAuth2 client secret + datasource password as a plain `Secret`. Replace with a Key Vault SecretProviderClass for prod (see below). |
| [`deployment.yaml`](deployment.yaml)       | Two replicas of the GHCR image with readiness/liveness probes against `/actuator/health/{readiness,liveness}`, non-root securityContext. |
| [`service.yaml`](service.yaml)             | ClusterIP service on `:80` → pod `:8080`.                                                                                          |
| [`ingress.yaml`](ingress.yaml)             | AGIC ingress with TLS, ssl-redirect, and cookie-based affinity (so the OAuth2 session cookie sticks to one pod through the flow).  |
| [`kustomization.yaml`](kustomization.yaml) | Bundles all of the above for `kubectl apply -k`.                                                                                   |

## Prerequisites

- An AKS cluster with the **AGIC add-on** enabled and a public Application Gateway in front of it.
- A DNS record (`dashboard.example.com` in the example) pointing at the App Gateway's public IP, and a TLS secret named `dashboard-tls` in the `spring-batch-dashboard` namespace (cert-manager, App Gateway-managed, or a manually-loaded `kubernetes.io/tls` secret all work).
- A reachable Spring Batch metadata database (the example assumes a Postgres at `postgres.prod.svc.cluster.local:5432`; swap for your own JDBC URL).
- A **GitHub OAuth App** with callback URL `https://dashboard.example.com/login/oauth2/code/github` (substitute your hostname). The OAuth app's client id and client secret go into [`configmap.yaml`](configmap.yaml) and [`secret.yaml`](secret.yaml) respectively.

The GHCR image is published from this repo's [release workflow](../../.github/workflows/release.yml) — no image pull secret is required for the public image.

## Apply

```bash
# 1. Edit the placeholder values (look for "replace-me" / "example.com").
$EDITOR deploy/aks/configmap.yaml deploy/aks/secret.yaml deploy/aks/ingress.yaml

# 2. Apply the bundle.
kubectl apply -k deploy/aks/

# 3. Watch the rollout.
kubectl -n spring-batch-dashboard rollout status deploy/dashboard
```

Once the ingress's backend is healthy, browse to `https://dashboard.example.com/` and sign in with GitHub.

## Extending the example

### Adding more datasources

The manifest declares a single `app.datasources[0]`; the backend supports any number of entries and any mix of POSTGRESQL / MYSQL / ORACLE. Append additional indexed blocks to [`configmap.yaml`](configmap.yaml) (and the matching password to [`secret.yaml`](secret.yaml)):

```yaml
APP_DATASOURCES_1_NAME: staging
APP_DATASOURCES_1_TYPE: MYSQL
APP_DATASOURCES_1_URL: jdbc:mysql://staging-db.staging.svc.cluster.local:3306/batch
APP_DATASOURCES_1_USERNAME: batch_reader
# APP_DATASOURCES_1_PASSWORD goes in secret.yaml

APP_DATASOURCES_2_NAME: dr
APP_DATASOURCES_2_TYPE: ORACLE
APP_DATASOURCES_2_URL: jdbc:oracle:thin:@//dr-db.dr.svc.cluster.local:1521/BATCHPDB
APP_DATASOURCES_2_USERNAME: batch_reader
APP_DATASOURCES_2_SCHEMA: BATCH_PROD       # honored on Oracle; ignored on MySQL
```

Each entry shows up in the SPA's environment selector and routes via `X-Environment`. See the [Datasources section of the project README](../../README.md#datasources) for the YAML reference and the Hibernate caveat that affects mixed-engine deployments.

### Adding more OAuth2 providers

Add another `spring.security.oauth2.client.registration.<id>.*` block per provider, and a matching `app.oauth2.buttons.<id>.*` block for the login-page button. OIDC providers (Okta, Azure AD, Auth0) also need a `spring.security.oauth2.client.provider.<id>.issuer-uri`.

Because the dashboard maps a single user-attribute set onto its `/api/auth/me` shape, mixing GitHub (whose user id is `login`) with OIDC providers (whose user id is `email`) requires picking the lowest-common-denominator attribute name:

```yaml
APP_AUTH_ATTRIBUTES_LOGIN: email
APP_AUTH_ATTRIBUTES_NAME: name
APP_AUTH_ATTRIBUTES_AVATAR_URL: picture
```

See the [Authentication section of the project README](../../README.md#authentication) for the full property list.

## Sessions and replica count

The dashboard uses Spring Security's default in-memory session store (a `JSESSIONID` cookie per JVM). With two replicas behind AGIC, the ingress's `cookie-based-affinity` annotation pins each client to one pod for the lifetime of the affinity cookie, so steady-state traffic is fine. The tradeoff: a pod restart (rolling update, node drain, OOM) logs that pod's users out and they have to re-authenticate. Acceptable for a read-only dashboard; if you need sessions to survive restarts, move them into a shared store (e.g. `spring-session-data-redis` + Azure Cache for Redis).

## Production hardening

The example uses a plain `Secret`. For production on AKS, swap it for the **Secrets Store CSI driver** with **Workload Identity** so the pod pulls credentials from Azure Key Vault at runtime:

1. Enable the secrets-store CSI add-on on the cluster (`az aks enable-addons --addons azure-keyvault-secrets-provider …`).
2. Set up Workload Identity on the cluster and federate a user-assigned managed identity to the dashboard's ServiceAccount.
3. Grant that identity `get`/`list` on the Key Vault holding `github-client-secret` and the database password.
4. Define a `SecretProviderClass` with `syncSecret: true` so the CSI driver mirrors the Key Vault values into a Kubernetes `Secret` (let it have the same name as `dashboard-secrets`), then drop [`secret.yaml`](secret.yaml) from `kustomization.yaml`.
5. Mount the CSI volume in [`deployment.yaml`](deployment.yaml) so the driver runs.

The `envFrom: secretRef: dashboard-secrets` reference in [`deployment.yaml`](deployment.yaml) doesn't change — only the source of the `Secret` does. AKS docs: <https://learn.microsoft.com/azure/aks/csi-secrets-store-driver>.

## Useful resources

### Azure / AKS

- [Azure Kubernetes Service (AKS) overview](https://learn.microsoft.com/azure/aks/intro-kubernetes) — managed Kubernetes on Azure; cluster lifecycle, node pools, networking, integrations.
- [Application Gateway Ingress Controller (AGIC)](https://learn.microsoft.com/azure/application-gateway/ingress-controller-overview) — turns Kubernetes `Ingress` resources into Azure Application Gateway routing rules. Source for the `appgw.ingress.kubernetes.io/*` annotations used in [`ingress.yaml`](ingress.yaml).
- [AGIC annotation reference](https://azure.github.io/application-gateway-kubernetes-ingress/annotations/) — exhaustive list of `appgw.ingress.kubernetes.io/*` annotations (TLS redirect, cookie affinity, rewrite rules, WAF, health probes).
- [Secrets Store CSI driver on AKS](https://learn.microsoft.com/azure/aks/csi-secrets-store-driver) — mounts Azure Key Vault secrets into pods at runtime, optionally syncing to a Kubernetes `Secret`. The recommended replacement for [`secret.yaml`](secret.yaml) in production.
- [Azure Workload Identity](https://learn.microsoft.com/azure/aks/workload-identity-overview) — federates a Kubernetes ServiceAccount to an Azure AD identity, so pods authenticate to Azure resources (like Key Vault) without long-lived secrets.
- [Azure Key Vault](https://learn.microsoft.com/azure/key-vault/general/overview) — managed secret/cert/key store; the upstream source the CSI driver pulls from.
- [Azure Cache for Redis](https://learn.microsoft.com/azure/azure-cache-for-redis/cache-overview) — managed Redis; what you'd back `spring-session-data-redis` with if you wanted sessions to survive pod restarts (see [Sessions and replica count](#sessions-and-replica-count)).
- [cert-manager](https://cert-manager.io/docs/) — automates issuance and renewal of TLS certificates inside the cluster (Let's Encrypt, ACME, internal CAs); produces the `kubernetes.io/tls` Secret referenced by [`ingress.yaml`](ingress.yaml).

### Kubernetes

- [Kustomize reference](https://kubectl.docs.kubernetes.io/references/kustomize/) — the `kubectl apply -k` engine. Bases, overlays, patches, image transformers, ConfigMap/Secret generators.
- [Configure liveness, readiness, and startup probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/) — semantics of the three probe kinds and how each affects pod lifecycle decisions.
- [Pod security context](https://kubernetes.io/docs/tasks/configure-pod-container/security-context/) — basis for the non-root / read-only-root / drop-all-capabilities settings in [`deployment.yaml`](deployment.yaml).
- [Ingress concept](https://kubernetes.io/docs/concepts/services-networking/ingress/) — how `Ingress` resources route external HTTP(S) traffic to in-cluster Services.

### Spring Boot

- [Externalized configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html) — the property-source precedence order and the relaxed-binding rules that let the env vars in [`configmap.yaml`](configmap.yaml) override `application.yml`.
- [Kubernetes probes with Actuator](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html#actuator.endpoints.kubernetes-probes) — the `/actuator/health/{readiness,liveness}` groups the [`deployment.yaml`](deployment.yaml) probes hit, and the auto-detection that flips them on inside K8s.
- [OAuth2 client configuration](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html) — registration vs. provider properties, common-provider defaults (GitHub, Google, Facebook, Okta), redirect-uri template.
- [Forward headers handling](https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.embedded-container.forward-headers) — what `server.forward-headers-strategy` does and why it's required when running behind an ingress that terminates TLS.
