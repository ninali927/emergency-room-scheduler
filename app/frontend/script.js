document.addEventListener('alpine:init', () => {
  Alpine.data('erScheduler', () => ({
    // Tab state
    activeTab: 'scheduler',

    // Connection state
    isConnected: false,
    serverInfo: '',
    ws: null,  // WebSocket connection for ER scheduler

    // ER state
    erState: {
      waitingCount: 0,
      totalRegistered: 0,
      treeHeight: 0,
      isEmpty: true
    },

    // Visualization state
    vizConnected: false,
    vizWs: null,
    vizState: {
      snapshotCount: 0,
      treeSize: 0,
      treeHeight: 0
    },
    currentSnapshot: null,
    currentSnapshotIndex: 0,
    snapshotList: [],

    // Visualization forms
    vizForm: {
      name: '',
      severity: '3',
      condition: '',
      arrivalTime: '',
      removeTime: ''
    },

    // Bulk insert for visualization
    vizBulkInsert: {
      count: 5
    },

    // Visualization messages
    vizMessages: [],
    vizMessageIdCounter: 0,

    // Forms
    newPatient: {
      name: '',
      severity: '3',
      condition: ''
    },

    bulkRegister: {
      count: 5
    },

    updateForm: {
      patientId: '',
      severity: '3'
    },

    queryForm: {
      rangeStart: 100,
      rangeEnd: 200,
      rankTime: 100,
      kArrival: 1,
      kSevere: 1,
      removeId: ''
    },

    // Random patient data
    randomNames: [
      'Emma Johnson', 'Liam Smith', 'Olivia Williams', 'Noah Brown',
      'Ava Jones', 'Ethan Garcia', 'Sophia Martinez', 'Mason Rodriguez',
      'Isabella Davis', 'William Miller', 'Mia Wilson', 'James Anderson'
    ],

    randomConditions: [
      'Broken arm', 'Chest pain', 'Severe headache', 'Abdominal pain',
      'Shortness of breath', 'High fever', 'Deep laceration', 'Allergic reaction',
      'Sprained ankle', 'Back pain', 'Concussion', 'Burn injury'
    ],

    // Results
    results: {
      visible: false,
      message: '',
      patients: null,
      patient: null,
      numericValue: null
    },

    // Messages (for notifications)
    messages: [],
    messageIdCounter: 0,

    /**
     * Computed: Check if snapshots exist
     */
    get hasSnapshots() {
      return this.snapshotList && this.snapshotList.length > 0;
    },

    /**
     * Initialize the application
     */
    init() {
      this.connectERWebSocket();
      this.connectVisualizationWebSocket();
    },

    /**
     * Connect to ER Scheduler WebSocket
     */
    connectERWebSocket() {
      const wsUrl = 'ws://localhost:7001/er';
      this.ws = new WebSocket(wsUrl);

      this.ws.onopen = () => {
        this.isConnected = true;
        this.showMessage('Connected to ER Scheduler', 'success');
      };

      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          this.handleWebSocketMessage(message);
        } catch (e) {
          console.error('Error parsing WebSocket message:', e);
        }
      };

      this.ws.onclose = () => {
        this.isConnected = false;
        this.showMessage('Disconnected from ER Scheduler', 'error');
        // Try to reconnect after 3 seconds
        setTimeout(() => this.connectERWebSocket(), 3000);
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        this.showMessage('WebSocket connection error', 'error');
      };
    },


    /**
     * Handle incoming WebSocket messages
     */
    handleWebSocketMessage(message) {
      console.log('Received:', message);

      switch (message.type) {
        case 'connected':
          this.serverInfo = message.data?.serverInfo || '';
          this.erState = message.data?.erState || this.erState;
          break;

        case 'patient_registered':
          this.updateERState(message.data?.erState);
          this.showMessage(message.message, 'success');
          this.clearRegistrationForm();
          break;

        case 'patient_treated':
          this.updateERState(message.data?.erState);
          // Merge arrivalTime into patient object if available
          const treatedPatient = message.data?.patient;
          if (treatedPatient && message.data?.arrivalTime !== undefined) {
            treatedPatient.arrivalTime = message.data.arrivalTime;
          }
          this.showPatientResult(treatedPatient, message.message);
          break;

        case 'peek_result':
          // Merge arrivalTime into patient object if available
          const peekedPatient = message.data?.patient;
          if (peekedPatient && message.data?.arrivalTime !== undefined) {
            peekedPatient.arrivalTime = message.data.arrivalTime;
          }
          this.showPatientResult(peekedPatient, message.message);
          break;

        case 'severity_updated':
          this.updateERState(message.data?.erState);
          this.showMessage(message.message, 'success');
          break;

        case 'range_search_result':
          this.showPatientsResult(message.data?.patients, message.message);
          break;

        case 'rank_query_result':
          this.showNumericResult(message.data?.rank, message.message);
          break;

        case 'kth_arrival_result':
          // Merge arrivalTime into patient object if available
          const kthArrivalPatient = message.data?.patient;
          if (kthArrivalPatient && message.data?.arrivalTime !== undefined) {
            kthArrivalPatient.arrivalTime = message.data.arrivalTime;
          }
          this.showPatientResult(kthArrivalPatient, message.message);
          break;

        case 'kth_severe_result':
          // Merge arrivalTime into patient object if available
          const kthSeverePatient = message.data?.patient;
          if (kthSeverePatient && message.data?.arrivalTime !== undefined) {
            kthSeverePatient.arrivalTime = message.data.arrivalTime;
          }
          this.showPatientResult(kthSeverePatient, message.message);
          break;

        case 'patient_removed':
          this.updateERState(message.data?.erState);
          this.showMessage(message.message, 'success');
          break;

        case 'all_patients_result':
          this.showPatientsResult(message.data?.patients, message.message);
          break;

        case 'er_cleared':
          this.updateERState(message.data?.erState);
          this.showMessage(message.message, 'success');
          this.clearResults();
          break;

        case 'no_patients':
          this.updateERState(message.data?.erState);
          this.showMessage(message.message, 'info');
          break;

        case 'error':
          this.showMessage(message.message, 'error');
          break;

        case 'ping':
          // Respond to keepalive ping
          this.sendMessage('pong', 'keepalive');
          break;

        default:
          console.log('Unhandled message type:', message.type);
      }
    },

    /**
     * Send a message to the backend
     */
    sendMessage(type, message, data = {}) {
      if (!this.isConnected) {
        this.showMessage('Not connected to server', 'error');
        return;
      }

      const msg = {
        type,
        message,
        data,
        timestamp: Date.now()
      };

      // Use stored WebSocket reference
      if (this.ws && this.ws.send) {
        this.ws.send(JSON.stringify(msg));
      } else {
        console.error('WebSocket not available:', this.ws);
        this.showMessage('WebSocket not available', 'error');
      }
    },

    /**
     * Register a patient manually
     */
    registerPatient() {
      this.sendMessage('register_patient', 'Registering patient', {
        name: this.newPatient.name,
        severity: parseInt(this.newPatient.severity),
        condition: this.newPatient.condition
      });
    },

    /**
     * Generate random patient data and populate the form
     */
    generateRandomPatient() {
      // Pick random name
      const randomName = this.randomNames[Math.floor(Math.random() * this.randomNames.length)];

      // Pick random condition
      const randomCondition = this.randomConditions[Math.floor(Math.random() * this.randomConditions.length)];

      // Pick random severity (1-5)
      const randomSeverity = Math.floor(Math.random() * 5) + 1;

      // Populate the form fields
      this.newPatient.name = randomName;
      this.newPatient.severity = randomSeverity.toString();
      this.newPatient.condition = randomCondition;

      this.showMessage('Random patient generated - edit as needed and click Register', 'info');
    },

    /**
     * Register multiple random patients
     */
    registerMultipleRandom() {
      const count = parseInt(this.bulkRegister.count);

      if (count < 1 || count > 50) {
        this.showMessage('Please enter a number between 1 and 50', 'error');
        return;
      }

      for (let i = 0; i < count; i++) {
        // Generate random patient data
        const randomName = this.randomNames[Math.floor(Math.random() * this.randomNames.length)];
        const randomCondition = this.randomConditions[Math.floor(Math.random() * this.randomConditions.length)];
        const randomSeverity = Math.floor(Math.random() * 5) + 1;

        // Register the patient
        this.sendMessage('register_patient', `Registering patient ${i + 1} of ${count}`, {
          name: randomName,
          severity: randomSeverity,
          condition: randomCondition
        });
      }

      this.showMessage(`Registered ${count} random patient${count > 1 ? 's' : ''}`, 'success');
    },

    /**
     * Treat the next patient
     */
    treatPatient() {
      this.sendMessage('treat_patient', 'Treating next patient');
    },

    /**
     * Peek at the next patient
     */
    peekPatient() {
      this.sendMessage('peek_patient', 'Peeking at next patient');
    },

    /**
     * Update patient severity
     */
    updateSeverity() {
      this.sendMessage('update_severity', 'Updating severity', {
        patientId: parseInt(this.updateForm.patientId),
        severity: parseInt(this.updateForm.severity)
      });
    },

    /**
     * Range search
     */
    rangeSearch() {
      this.sendMessage('range_search', 'Searching range', {
        startTime: parseInt(this.queryForm.rangeStart),
        endTime: parseInt(this.queryForm.rangeEnd)
      });
    },

    /**
     * Rank query
     */
    rankQuery() {
      this.sendMessage('rank_query', 'Querying rank', {
        arrivalTime: parseInt(this.queryForm.rankTime)
      });
    },

    /**
     * K-th by arrival
     */
    kthByArrival() {
      this.sendMessage('kth_by_arrival', 'Finding k-th by arrival', {
        k: parseInt(this.queryForm.kArrival)
      });
    },

    /**
     * K-th most severe
     */
    kthMostSevere() {
      this.sendMessage('kth_most_severe', 'Finding k-th most severe', {
        k: parseInt(this.queryForm.kSevere)
      });
    },

    /**
     * Remove from queue
     */
    removeFromQueue() {
      this.sendMessage('remove_from_queue', 'Removing patient', {
        patientId: parseInt(this.queryForm.removeId)
      });
    },

    /**
     * Get all patients
     */
    getAllPatients() {
      this.sendMessage('get_all_patients', 'Getting all patients');
    },

    /**
     * Clear all patients
     */
    clearAll() {
      if (confirm('Are you sure you want to clear all patients?')) {
        this.sendMessage('clear_all', 'Clearing all patients');
      }
    },

    /**
     * Update ER state from server
     */
    updateERState(state) {
      if (state) {
        this.erState = state;
      }
    },

    /**
     * Show a single patient result
     */
    showPatientResult(patient, message) {
      this.results = {
        visible: true,
        message: message,
        patient: patient,
        patients: null,
        numericValue: null
      };
    },

    /**
     * Show multiple patients result
     */
    showPatientsResult(patients, message) {
      this.results = {
        visible: true,
        message: message,
        patients: patients,
        patient: null,
        numericValue: null
      };
    },

    /**
     * Show numeric result
     */
    showNumericResult(value, message) {
      this.results = {
        visible: true,
        message: message,
        numericValue: value,
        patients: null,
        patient: null
      };
    },

    /**
     * Clear results
     */
    clearResults() {
      this.results = {
        visible: false,
        message: '',
        patients: null,
        patient: null,
        numericValue: null
      };
    },

    /**
     * Clear registration form
     */
    clearRegistrationForm() {
      this.newPatient = {
        name: '',
        severity: '3',
        condition: ''
      };
    },

    /**
     * Show a notification message
     */
    showMessage(text, type = 'info') {
      const id = this.messageIdCounter++;
      const message = {
        id,
        text,
        type,
        visible: true
      };

      this.messages.push(message);

      // Auto-remove after 5 seconds
      setTimeout(() => {
        this.removeMessage(id);
      }, 5000);
    },

    /**
     * Remove a message by ID
     */
    removeMessage(id) {
      const index = this.messages.findIndex(m => m.id === id);
      if (index !== -1) {
        this.messages[index].visible = false;
        setTimeout(() => {
          this.messages = this.messages.filter(m => m.id !== id);
        }, 300);
      }
    },

    // ========================================
    // VISUALIZATION METHODS
    // ========================================

    /**
     * Connect to visualization WebSocket
     */
    connectVisualizationWebSocket() {
      const wsUrl = 'ws://localhost:7001/visualization';
      this.vizWs = new WebSocket(wsUrl);

      this.vizWs.onopen = () => {
        this.vizConnected = true;
        this.showVizMessage('Connected to AVL Visualization', 'success');
      };

      this.vizWs.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          this.handleVisualizationMessage(message);
        } catch (e) {
          console.error('Error parsing visualization message:', e);
        }
      };

      this.vizWs.onclose = () => {
        this.vizConnected = false;
        this.showVizMessage('Disconnected from visualization', 'error');
      };

      this.vizWs.onerror = (error) => {
        console.error('Visualization WebSocket error:', error);
        this.vizConnected = false;
        this.showVizMessage('Connection error', 'error');
      };
    },

    /**
     * Handle visualization WebSocket messages
     */
    handleVisualizationMessage(message) {
      console.log('Viz received:', message);

      switch (message.type) {
        case 'viz_connected':
          console.log('Visualization connected:', message.data);
          break;

        case 'viz_insert_success':
          this.updateVizState(message.data);
          this.showVizMessage(message.message, 'success');
          this.clearVizForm();
          break;

        case 'viz_remove_success':
          this.updateVizState(message.data);
          this.showVizMessage(message.message, 'success');
          break;

        case 'viz_remove_failed':
          this.showVizMessage(message.message, 'error');
          break;

        case 'viz_snapshots':
          this.snapshotList = message.data?.snapshots || [];
          this.vizState.snapshotCount = message.data?.count || 0;
          if (this.snapshotList.length > 0) {
            this.currentSnapshotIndex = this.snapshotList.length - 1;
            this.currentSnapshot = this.snapshotList[this.currentSnapshotIndex];
            this.renderTree();
          }
          break;

        case 'viz_snapshot':
          this.currentSnapshot = message.data?.snapshot;
          this.currentSnapshotIndex = message.data?.index || 0;
          this.renderTree();
          break;

        case 'viz_current_snapshot':
          this.updateVizState(message.data);
          break;

        case 'viz_reset_success':
          this.updateVizState(message.data);
          this.showVizMessage(message.message, 'success');
          this.vizGetAllSnapshots();
          break;

        case 'viz_clear_success':
          this.vizState.snapshotCount = message.data?.snapshotCount || 0;
          this.snapshotList = [];
          this.currentSnapshot = null;
          this.currentSnapshotIndex = 0;
          this.renderTree();
          this.showVizMessage(message.message, 'success');
          break;

        case 'error':
          this.showVizMessage(message.message, 'error');
          break;

        case 'ping':
          this.sendVizMessage('pong', 'keepalive');
          break;

        default:
          console.log('Unhandled viz message type:', message.type);
      }
    },

    /**
     * Send visualization message
     */
    sendVizMessage(type, message, data = {}) {
      if (!this.vizConnected || !this.vizWs) {
        this.showVizMessage('Not connected to visualization server', 'error');
        return;
      }

      const msg = {
        type,
        message,
        data,
        timestamp: Date.now()
      };

      this.vizWs.send(JSON.stringify(msg));
    },

    /**
     * Update visualization state
     */
    updateVizState(data) {
      if (data?.snapshotCount !== undefined) {
        this.vizState.snapshotCount = data.snapshotCount;
      }
      if (data?.currentSnapshot) {
        this.currentSnapshot = data.currentSnapshot;

        // FIX: Copy the size and height from the snapshot into the vizState
        this.vizState.treeSize = data.currentSnapshot.treeSize || 0;
        this.vizState.treeHeight = data.currentSnapshot.treeHeight || 0;

        this.currentSnapshotIndex = this.vizState.snapshotCount - 1;
        this.renderTree();
        // Refresh snapshot list
        this.vizGetAllSnapshots();
      }
    },

    /**
     * Insert node
     */
    vizInsertNode() {
      const data = {
        name: this.vizForm.name,
        severity: parseInt(this.vizForm.severity),
        condition: this.vizForm.condition
      };

      if (this.vizForm.arrivalTime) {
        data.arrivalTime = parseInt(this.vizForm.arrivalTime);
      }

      this.sendVizMessage('viz_insert', 'Inserting node', data);
    },

    /**
     * Generate random patient data for visualization
     */
    generateRandomVizPatient() {
      const randomName = this.randomNames[Math.floor(Math.random() * this.randomNames.length)];
      const randomCondition = this.randomConditions[Math.floor(Math.random() * this.randomConditions.length)];
      const randomSeverity = Math.floor(Math.random() * 5) + 1;

      this.vizForm.name = randomName;
      this.vizForm.severity = randomSeverity.toString();
      this.vizForm.condition = randomCondition;
      this.vizForm.arrivalTime = '';

      this.showVizMessage('Random patient generated - edit as needed and click Insert Node', 'info');
    },

    /**
     * Insert multiple random nodes
     */
    vizInsertMultipleRandom() {
      const count = parseInt(this.vizBulkInsert.count);

      if (count < 1 || count > 50) {
        this.showVizMessage('Please enter a number between 1 and 50', 'error');
        return;
      }

      for (let i = 0; i < count; i++) {
        const randomName = this.randomNames[Math.floor(Math.random() * this.randomNames.length)];
        const randomCondition = this.randomConditions[Math.floor(Math.random() * this.randomConditions.length)];
        const randomSeverity = Math.floor(Math.random() * 5) + 1;

        this.sendVizMessage('viz_insert', `Inserting node ${i + 1} of ${count}`, {
          name: randomName,
          severity: randomSeverity,
          condition: randomCondition
        });
      }

      this.showVizMessage(`Inserted ${count} random node${count > 1 ? 's' : ''}`, 'success');
    },

    /**
     * Remove node
     */
    vizRemoveNode() {
      this.sendVizMessage('viz_remove', 'Removing node', {
        arrivalTime: parseInt(this.vizForm.removeTime)
      });
    },

    /**
     * Reset snapshot history
     */
    vizResetHistory() {
      if (confirm('Reset snapshot history? This will keep the current tree but clear all history.')) {
        this.sendVizMessage('viz_reset', 'Resetting history');
      }
    },

    /**
     * Clear tree
     */
    vizClearTree() {
      if (confirm('Clear the entire tree? This will remove all nodes and snapshots.')) {
        this.sendVizMessage('viz_clear', 'Clearing tree');
      }
    },

    /**
     * Get all snapshots
     */
    vizGetAllSnapshots() {
      this.sendVizMessage('viz_get_snapshots', 'Getting snapshots');
    },

    /**
     * Go to specific snapshot
     */
    vizGotoSnapshot(index) {
      if (index >= 0 && index < this.snapshotList.length) {
        this.currentSnapshotIndex = index;
        this.currentSnapshot = this.snapshotList[index];
        this.renderTree();
      }
    },

    /**
     * Go to previous snapshot
     */
    vizPrevSnapshot() {
      if (this.currentSnapshotIndex > 0) {
        this.vizGotoSnapshot(this.currentSnapshotIndex - 1);
      }
    },

    /**
     * Go to next snapshot
     */
    vizNextSnapshot() {
      if (this.currentSnapshotIndex < this.snapshotList.length - 1) {
        this.vizGotoSnapshot(this.currentSnapshotIndex + 1);
      }
    },

    /**
     * Clear visualization form
     */
    clearVizForm() {
      this.vizForm = {
        name: '',
        severity: '3',
        condition: '',
        arrivalTime: '',
        removeTime: ''
      };
    },

    /**
     * Show visualization message
     */
    showVizMessage(text, type = 'info') {
      const id = this.vizMessageIdCounter++;
      const message = {
        id,
        text,
        type,
        visible: true
      };

      this.vizMessages.push(message);

      setTimeout(() => {
        this.removeVizMessage(id);
      }, 5000);
    },

    /**
     * Remove visualization message
     */
    removeVizMessage(id) {
      const index = this.vizMessages.findIndex(m => m.id === id);
      if (index !== -1) {
        this.vizMessages[index].visible = false;
        setTimeout(() => {
          this.vizMessages = this.vizMessages.filter(m => m.id !== id);
        }, 300);
      }
    },

    /**
     * Render the tree
     */
    renderTree() {
      const svg = this.$refs.treeSvg;
      if (!svg) return;

      // Clear existing content
      svg.innerHTML = '';

      if (!this.currentSnapshot || !this.currentSnapshot.tree) {
        // Draw empty state
        const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        text.setAttribute('x', '400');
        text.setAttribute('y', '300');
        text.setAttribute('text-anchor', 'middle');
        text.setAttribute('fill', '#94a3b8');
        text.setAttribute('font-size', '18');
        text.textContent = 'Tree is empty';
        svg.appendChild(text);
        return;
      }

      const tree = this.currentSnapshot.tree;
      const positions = this.calculateNodePositions(tree);

      // Draw edges first (so they appear behind nodes)
      this.drawEdges(svg, positions);

      // Draw nodes
      this.drawNodes(svg, positions);
    },

    /**
     * Calculate positions for all nodes
     */
    calculateNodePositions(tree) {
      const positions = [];
      const nodeWidth = 60;
      const levelHeight = 80;
      const padding = 20;

      const calculateInOrder = (node) => {
        if (!node) return 0;
        const left = calculateInOrder(node.left);
        const count = 1;
        const right = calculateInOrder(node.right);
        return left + count + right;
      };

      const totalNodes = calculateInOrder(tree);
      const totalWidth = Math.max(totalNodes * nodeWidth, 800);

      let inOrderIndex = 0;

      const traverse = (node, depth = 0) => {
        if (!node) return;

        // Left subtree
        traverse(node.left, depth + 1);

        // Current node
        const x = padding + (inOrderIndex + 0.5) * (totalWidth - 2 * padding) / totalNodes;
        const y = padding + depth * levelHeight + 40;

        positions.push({
          node: node,
          x: x,
          y: y
        });

        inOrderIndex++;

        // Right subtree
        traverse(node.right, depth + 1);
      };

      traverse(tree);
      return positions;
    },

    /**
     * Draw edges between nodes
     */
    drawEdges(svg, positions) {
      const posMap = new Map();
      positions.forEach(p => {
        posMap.set(p.node, { x: p.x, y: p.y });
      });

      positions.forEach(({ node, x, y }) => {
        if (node.left) {
          const childPos = posMap.get(node.left);
          if (childPos) {
            const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
            line.setAttribute('x1', x);
            line.setAttribute('y1', y);
            line.setAttribute('x2', childPos.x);
            line.setAttribute('y2', childPos.y);
            line.setAttribute('stroke', '#cbd5e1');
            line.setAttribute('stroke-width', '2');
            svg.appendChild(line);
          }
        }

        if (node.right) {
          const childPos = posMap.get(node.right);
          if (childPos) {
            const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
            line.setAttribute('x1', x);
            line.setAttribute('y1', y);
            line.setAttribute('x2', childPos.x);
            line.setAttribute('y2', childPos.y);
            line.setAttribute('stroke', '#cbd5e1');
            line.setAttribute('stroke-width', '2');
            svg.appendChild(line);
          }
        }
      });
    },

    /**
     * Draw nodes
     */
    drawNodes(svg, positions) {
      positions.forEach(({ node, x, y }) => {
        // Draw circle
        const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
        circle.setAttribute('cx', x);
        circle.setAttribute('cy', y);
        circle.setAttribute('r', '25');
        circle.setAttribute('fill', '#2563eb');
        circle.setAttribute('stroke', '#1e40af');
        circle.setAttribute('stroke-width', '2');
        svg.appendChild(circle);

        // Draw arrival time
        const text = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        text.setAttribute('x', x);
        text.setAttribute('y', y + 5);
        text.setAttribute('text-anchor', 'middle');
        text.setAttribute('fill', 'white');
        text.setAttribute('font-size', '14');
        text.setAttribute('font-weight', 'bold');
        text.textContent = node.arrivalTime;
        svg.appendChild(text);

        // Draw height/balance factor below
        const info = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        info.setAttribute('x', x);
        info.setAttribute('y', y + 40);
        info.setAttribute('text-anchor', 'middle');
        info.setAttribute('fill', '#64748b');
        info.setAttribute('font-size', '10');
        info.textContent = `h:${node.height} bf:${node.balanceFactor}`;
        svg.appendChild(info);
      });
    }
  }));
});
