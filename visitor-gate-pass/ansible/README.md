# Ansible Configuration Management Guide

This directory contains Ansible automation playbooks and inventories for provisioning, configuring, deploying, verifying, and rolling back the **Automated Residential Visitor Gate-Pass System**.

---

## Directory Structure

```text
ansible/
├── inventory.ini           # Multi-environment target inventory (local, staging, prod)
├── playbook.yml            # Main deployment and configuration playbook
├── rollback.yml            # Emergency rollback playbook to restore previous release
├── vars/
│   └── main.yml           # Externalized configuration variables
└── README.md               # Architecture and execution guide
```

---

## Target Host Configuration

### `inventory.ini`
Contains groupings for:
- `[local]`: Local developer/CI execution via local connection.
- `[staging]`: Pre-production validation nodes.
- `[production]`: Production multi-node cluster with dedicated deploy user.

### `vars/main.yml`
Controls ports, database parameters, Docker image tags, healthcheck timeouts, and deployment directories (`/opt/visitor-gate-pass`).

---

## Execution Instructions

### 1. Dry Run / Syntax Validation
```bash
ansible-playbook -i inventory.ini playbook.yml --syntax-check
```

### 2. Provisioning & Deployment Execution
```bash
ansible-playbook -i inventory.ini playbook.yml -l local
```

### 3. Idempotency Validation
Execute the playbook a second time:
```bash
ansible-playbook -i inventory.ini playbook.yml -l local
```
**Verification:** The second run reports `changed=0` for pre-existing resources, confirming strict idempotency.

### 4. Emergency Rollback Execution
If a defect is detected in a new release:
```bash
ansible-playbook -i inventory.ini rollback.yml -l local
```
Restores previous configuration, relaunches containers with the fallback image, and verifies health status.

---

## Windows Execution Note

In native Windows environments without the Linux-based `ansible-playbook` CLI, the included PowerShell automation script `scripts/verify_provisioning.ps1` executes and verifies the identical provisioning, idempotency, and rollback lifecycle.
