# Git Collaboration & Conflict Resolution Demonstration (Week 6 Deliverable)

**Project:** Automated Residential Visitor Gate-Pass System  
**Deliverable Code:** DEL-W06-GCR  
**Version:** 1.0.0  

---

## 1. Objective
Demonstrate hands-on proficiency in Git collaboration by creating, identifying, and resolving a real Git merge conflict on a secondary feature branch before merging into the integration baseline.

---

## 2. Simulated Conflict Scenario

### The Conflict Context:
Two developers working concurrently on sprint branches modified `docker-compose.yml` simultaneously:
1. **Developer A (on `feature/db-port-optimization`):** Changed MySQL port mapping from `3306:3306` to `3307:3306` to prevent collisions with host machine MySQL instances.
2. **Developer B (on `feature/visitor-pass-crud`):** Added container health checks and custom network aliases on line 22 of `docker-compose.yml`.

### Creating the Conflict in Git:
```bash
# Branch A commits changes to docker-compose.yml
git checkout develop
git checkout -b feature/db-port-optimization
# Edit line 22 of docker-compose.yml: "3307:3306"
git commit -am "chore(db): update host port mapping to 3307"

# Branch B also edited the same lines on develop
git checkout develop
# Edit line 22 of docker-compose.yml with different parameters
git commit -am "feat(db): update database port and environment config"

# Trigger merge conflict:
git merge feature/db-port-optimization
```

### Git Conflict Output:
```text
Auto-merging docker-compose.yml
CONFLICT (content): Merge conflict in docker-compose.yml
Automatic merge failed; fix conflicts and then commit the result.
```

---

## 3. Conflict Inspection (`git status` & `git diff`)

```text
<<<<<<< HEAD
    ports:
      - "3306:3306"
    environment:
      - MYSQL_DATABASE=gatepass_db
=======
    ports:
      - "3307:3306"
    environment:
      - MYSQL_DATABASE=gatepass_db
>>>>>>> feature/db-port-optimization
```

---

## 4. Conflict Resolution Procedure

1. **Analysis & Decision:**
   - Both developers agreed that port `3307:3306` is mandatory for Windows developer machines running local MySQL services.
2. **Manual Resolution:**
   - Retained the `3307:3306` port mapping from `feature/db-port-optimization`.
   - Preserved `MYSQL_DATABASE=gatepass_db` and healthcheck directives from `HEAD`.
   - Removed Git conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`).
3. **Stage and Finalize Merge:**
   ```bash
   git add docker-compose.yml
   git commit -m "Merge branch 'feature/db-port-optimization' into develop: resolved port collision to 3307"
   ```

---

## 5. Verification
- Verified compose stack starts with `docker compose up -d`.
- Verified healthcheck returns healthy via `mysqladmin ping`.
- Preserved clean Git branch history and verified `git log --graph --oneline`.
