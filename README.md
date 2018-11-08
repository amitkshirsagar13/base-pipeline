# base-pipeline

Create RegSecret for Docker registry:

```
kubectl create secret docker-registry private-registry-key --docker-username="username" --docker-password="password" --docker-email="username@example.com" --docker-server="https://index.docker.io/v1/"

cat > regsecret.yml << EOF
apiVersion: v1
kind: Secret
metadata:
  name: private-registry-key
  namespace: default
data:
  .dockerconfigjson: UmVhbGbGxsbGxsbGxsbGxsbGxsbGxsbGxsbGx5eXl5eXl5eXl5eXl5eXl5eXl5eSBsbGxsbGxsbGxsbGxsbG9vb29vb29vb29vb29vb29vb29vb29vb29vb25ubm5ubm5ubm5ubm5ubm5ubm5ubm5ubmdnZ2dnZ2dnZ2dnZ2dnZ2dnZ2cgYXV0aCBrZXlzCg==
type: kubernetes.io/dockerconfigjson
EOF

kubectl apply -f regsecret.yml
```
