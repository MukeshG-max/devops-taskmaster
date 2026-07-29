<<<<<<< HEAD

=======
# DevOps TaskMaster

A full-stack task management dashboard built to demonstrate core AWS + Kubernetes DevOps skills — deployed on Amazon EKS with a complete CI/CD pipeline to ECR.

## 📋 Overview

TaskMaster is a task tracking application with a REST API backend, MySQL database, and an interactive dashboard frontend with real-time charts. The project was built end-to-end — from local development through containerization, cloud infrastructure provisioning, and production-style deployment on Kubernetes.

## 🛠️ Tech Stack

**Backend**

- Java 21, Spring Boot
- Spring Data JPA / Hibernate
- MySQL 8.0

**Frontend**

- HTML, CSS, JavaScript
- Chart.js (task distribution visualizations)

**Infrastructure & DevOps**

- AWS EKS (Elastic Kubernetes Service)
- Amazon ECR (container registry)
- Docker
- GitHub Actions (CI/CD)
- AWS EC2 Security Groups / Classic Load Balancer
- kubectl, eksctl

## 🏗️ Architecture

Browser
│
▼
AWS Classic Load Balancer (Service: type=LoadBalancer)
│
▼
EKS Worker Nodes (t3.small × 2)
│
├── Pod: taskmaster (Spring Boot app, port 8080)
│ │
│ ▼
└── Pod: mysql (MySQL 8.0, port 3306)

- The frontend is served directly from the Spring Boot app's static resources and communicates with the backend via REST (`/api/tasks`).
- MySQL runs as an in-cluster Kubernetes pod (not RDS) to avoid cross-region latency, since the database was originally provisioned in a different AWS region than the EKS cluster.
- The application is exposed externally via a Kubernetes `LoadBalancer` Service, provisioning a Classic ELB.

## ⚙️ Features

- Full CRUD REST API for tasks (`GET`, `POST`, `PUT`, `DELETE`)
- Dashboard with live task counts (Total / To Do / In Progress / Done)
- Doughnut chart and bar chart visualizations of task status distribution
- Add/Edit task modal with status assignment
- Containerized backend, deployed via Kubernetes manifests
- Automated build & push pipeline via GitHub Actions

## 🚀 CI/CD Pipeline

On every push to `main`:

1. Checkout code
2. Set up JDK 21
3. Build the Spring Boot app with Maven
4. Authenticate to AWS
5. Log in to Amazon ECR
6. Build the Docker image
7. Push the image to ECR with the `latest` tag

Workflow file: `.github/workflows/deploy.yml`

## ☸️ Kubernetes Setup

Manifests are in the `k8s/` directory:

| File              | Purpose                                                            |
| ----------------- | ------------------------------------------------------------------ |
| `mysql.yaml`      | MySQL Deployment + Service (ClusterIP)                             |
| `deployment.yaml` | TaskMaster app Deployment, pulling image from ECR                  |
| `service.yaml`    | LoadBalancer Service exposing the app externally on port 80 → 8080 |

### Resource limits

Both pods define explicit CPU/memory requests and limits, tuned to fit within the constraints of `t3.small` EKS worker nodes.

### Deploy commands

```bash
kubectl apply -f k8s/mysql.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl get pods
kubectl get svc taskmaster
```

## 🧩 Challenges & Solutions

This project involved real infrastructure troubleshooting, not just following a tutorial:

**1. EKS nodegroup failed on `t3.medium`**

- Root cause: AWS Free Tier only supports `t3.micro` / `t2.micro` instance types — `t3.medium` isn't Free Tier eligible, causing `CREATE_FAILED` on the CloudFormation stack.
- Fix: Switched to `t3.micro`, which then exposed a second issue.

**2. Pods stuck `Pending` / `Evicted` on `t3.micro`**

- Root cause: `t3.micro` has a hard limit of 4 pods per node (AWS ENI/IP address limit) and very little allocatable memory (~524Mi). System pods alone (`aws-node`, `kube-proxy`, CoreDNS) consumed nearly all available pod slots and memory, leaving no room for the application pods.
- Diagnosed via `kubectl describe nodes`, confirming actual allocatable resources rather than assuming.
- Fix: Upgraded nodegroup to `t3.small` (still low-cost, ~$0.02/hr per node), which resolved both the pod limit and memory constraints.

**3. Cross-region RDS**

- Initial RDS instance was provisioned in a different AWS region than the EKS cluster, which would introduce unnecessary latency and complexity for a demo project.
- Fix: Deployed MySQL as an in-cluster pod instead, keeping the entire stack within a single region and VPC.

**4. LoadBalancer timeout (`ERR_CONNECTION_TIMED_OUT`)**

- The ELB was correctly provisioned and pointed at healthy pod endpoints, but external requests still timed out.
- Root cause: The worker node security group did not allow inbound traffic on the Kubernetes NodePort range, so the ELB couldn't actually reach the nodes.
- Fix: Added an inbound rule to the node security group allowing TCP traffic on ports `30000–32767`, resolving external connectivity.

## 📈 What's Next (Planned Improvements)

- [ ] Helm chart packaging instead of raw manifests
- [ ] ALB Ingress Controller instead of Classic LoadBalancer
- [ ] Horizontal Pod Autoscaler (HPA) for automatic scaling
- [ ] AWS Secrets Manager + External Secrets Operator (remove hardcoded DB credentials)
- [ ] Terraform for cluster provisioning (replacing eksctl for full IaC)
- [ ] CloudWatch Container Insights and/or Prometheus + Grafana for monitoring
- [ ] Network Policies for pod-to-pod traffic restrictions

## 📂 Repository Structure

.
├── src/ # Spring Boot application source
├── k8s/ # Kubernetes manifests
│ ├── mysql.yaml
│ ├── deployment.yaml
│ └── service.yaml
├── Dockerfile
├── .github/workflows/
│ └── deploy.yml
└── README.md

## 🎯 Key Takeaways

This project demonstrates practical, hands-on experience with:

- Provisioning and managing an EKS cluster and nodegroups
- Diagnosing real Kubernetes scheduling and resource constraints
- Container image build/push automation via CI/CD
- Debugging AWS networking issues (security groups, load balancers, NodePorts)
- Making pragmatic architecture trade-offs (in-cluster MySQL vs. RDS) under real constraints
>>>>>>> 526fc4f (Add README, screenshots, and final k8s manifests)
