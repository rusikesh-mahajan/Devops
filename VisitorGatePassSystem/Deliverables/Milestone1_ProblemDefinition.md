# Milestone 1: Problem Definition and Scope

## Project: Automated Residential Visitor Gate-Pass System

### 1. Problem Statement
Managing visitors in residential complexes using traditional paper-based logbooks is inefficient, prone to errors, and poses significant security risks. Security personnel struggle with maintaining accurate records, verifying visitor identities in real-time, and communicating effectively with residents to approve or reject entries. This leads to long wait times at the gate, unauthorized access risks, and difficulty in retrieving historical visitor data for audits or investigations.

### 2. Target Users
- **Residents/Hosts:** Need to pre-approve expected visitors, receive real-time entry requests, and manage past visitor logs.
- **Security Personnel (Guards):** Need a fast, reliable interface to register new visitors, capture details, and seek resident approval.
- **Admin/Facility Managers:** Need to oversee security operations, manage user roles, and generate security reports.
- **Visitors:** Need a smooth, quick check-in experience without unnecessary delays.

### 3. Existing Pain Points
- **Manual Data Entry:** Paper logs are hard to read, easily damaged, and time-consuming.
- **Lack of Real-time Verification:** Guards cannot instantly verify if a resident is available or willing to allow a visitor.
- **Security Vulnerabilities:** No photographic or digital footprint of visitors; easy to fake entries.
- **Retrieval & Auditing:** Searching for a past visitor record takes hours and is often inaccurate.
- **Queuing at Gates:** Peak hours cause massive bottlenecks at the entry gates.

### 4. Stakeholders
- Residential Welfare Association (RWA) / Facility Management
- Security Agency / Guard Staff
- Residents / Tenants
- System Administrators / IT Support
- Local Law Enforcement (for audit/compliance purposes)

### 5. Measurable Success Criteria
- **Check-in Time:** Reduce average visitor check-in time from >3 minutes to <1 minute.
- **Digital Adoption:** 100% replacement of paper logs within 2 weeks of deployment.
- **Uptime & Reliability:** System availability of 99.9% to ensure no disruptions at the gate.
- **Resident Response Time:** 80% of resident approvals/rejections processed within 30 seconds of the request.
- **Searchability:** Instant retrieval of historical visitor logs based on date, name, or vehicle number.

### 6. Constraints
- **Technical constraints:** Must run efficiently on low-end devices (e.g., basic tablets or smartphones used by security guards).
- **Network constraints:** Needs to handle intermittent internet connectivity at the gate.
- **Budget constraints:** Strict budget for infrastructure; favoring open-source technologies and self-hosted/cloud-lite architectures.
- **Time constraints:** MVP must be delivered and operational within the 15-week timeline.

### 7. Approved 15-Week MVP Scope
The Minimum Viable Product (MVP) will focus exclusively on the core visitor management workflow:
1. **User Roles:** Admin, Resident, Security Guard.
2. **Visitor Registration:** Guards can create a new visitor record (Name, Phone, Purpose, Vehicle No., Host Resident).
3. **Role-Based Workflow:** 
   - Guard submits entry request.
   - Resident views request and updates status to "Approved" or "Rejected".
   - Guard updates status to "Checked-In" and later "Checked-Out".
4. **Dashboard & Search:** 
   - Summary dashboard showing current active visitors inside the premises.
   - Search functionality to retrieve visitor records by date, name, or status.
5. **Infrastructure & Deployment:** Complete automated CI/CD pipeline using Jenkins, Docker, and Ansible for reliable deployment.
