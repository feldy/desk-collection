function collectionApp() {
    return {
        debtors: [],
        filteredDebtors: [], // Separate to avoid getter complexity with initial state
        isDebtorModalOpen: false,
        isImportModalOpen: false,
        isManageModalOpen: false,
        selectedDebtor: null,
        currentLoans: [],
        currentInteractions: [],

        // Filters
        searchQuery: '',
        sortBy: 'name_asc',
        filterHasDebt: false,

        // Forms
        debtorForm: { name: '', phoneNumber: '', address: '' },
        loanForm: { amount: '', dueDate: '' },
        interactionForm: { type: 'CALL', notes: '' },
        showLoanForm: false,

        async init() {
            await this.fetchDebtors();
            this.updateFilteredDebtors();

            // Watchers for filters
            this.$watch('searchQuery', () => this.updateFilteredDebtors());
            this.$watch('sortBy', () => this.updateFilteredDebtors());
            this.$watch('filterHasDebt', () => this.updateFilteredDebtors());
            this.$watch('debtors', () => this.updateFilteredDebtors());
        },

        updateFilteredDebtors() {
            let result = [...this.debtors]; // Clone

            // 1. Search
            if (this.searchQuery) {
                const lower = this.searchQuery.toLowerCase();
                result = result.filter(d =>
                    d.name.toLowerCase().includes(lower) ||
                    d.phoneNumber.includes(lower)
                );
            }

            // 2. Filter
            if (this.filterHasDebt) {
                result = result.filter(d => this.calculateTotalDebt(d.loans) > 0);
            }

            // 3. Sort
            result.sort((a, b) => {
                if (this.sortBy === 'name_asc') return a.name.localeCompare(b.name);
                if (this.sortBy === 'name_desc') return b.name.localeCompare(a.name);

                const debtA = this.calculateTotalDebt(a.loans);
                const debtB = this.calculateTotalDebt(b.loans);

                if (this.sortBy === 'debt_desc') return debtB - debtA;
                if (this.sortBy === 'debt_asc') return debtA - debtB;
                return 0;
            });

            this.filteredDebtors = result;
        },

        // --- DATA FETCHING ---
        async fetchDebtors() {
            try {
                const res = await fetch('/api/debtors');
                this.debtors = await res.json();
            } catch (e) {
                console.error("Error fetching debtors", e);
            }
        },

        async fetchDebtorDetails(id) {
            try {
                const [loans, interactions] = await Promise.all([
                    fetch(`/api/debtors/${id}/loans`).then(r => r.json()),
                    fetch(`/api/debtors/${id}/interactions`).then(r => r.json())
                ]);
                this.currentLoans = loans;
                this.currentInteractions = interactions.sort((a, b) => new Date(b.interactionTime) - new Date(a.interactionTime));
            } catch (e) {
                console.error("Error fetching details", e);
            }
        },

        // --- ACTIONS ---
        async saveDebtor() {
            if (!this.debtorForm.name || !this.debtorForm.phoneNumber) return alert('Name and Phone are required');

            await fetch('/api/debtors', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(this.debtorForm)
            });

            this.closeDebtorModal();
            await this.fetchDebtors();
        },

        async deleteDebtor(id) {
            if (!confirm('Delete this debtor and all related data?')) return;
            await fetch(`/api/debtors/${id}`, { method: 'DELETE' });
            await this.fetchDebtors();
            await this.fetchDebtors();
        },

        async importDebtors() {
            const fileInput = this.$refs.importFile;
            if (!fileInput.files.length) return alert('Please select a file');

            const formData = new FormData();
            formData.append('file', fileInput.files[0]);

            try {
                const res = await fetch('/api/debtors/import', {
                    method: 'POST',
                    body: formData
                });

                if (res.ok) {
                    alert('Debtors imported successfully!');
                    this.closeImportModal();
                    await this.fetchDebtors();
                } else {
                    const text = await res.text();
                    alert('Failed to import: ' + text);
                }
            } catch (e) {
                console.error(e);
                alert('Error importing file');
            }
        },

        async saveLoan() {
            if (!this.loanForm.amount || !this.loanForm.dueDate) return;

            const payload = {
                amount: this.loanForm.amount,
                remainingAmount: this.loanForm.amount,
                dueDate: this.loanForm.dueDate,
                status: 'CURRENT'
            };

            await fetch(`/api/debtors/${this.selectedDebtor.id}/loans`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            this.loanForm = { amount: '', dueDate: '' };
            this.showLoanForm = false;
            await this.fetchDebtorDetails(this.selectedDebtor.id);
            await this.fetchDebtors(); // Update totals in main list
        },

        async saveInteraction() {
            if (!this.interactionForm.notes) return;

            await fetch(`/api/debtors/${this.selectedDebtor.id}/interactions`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(this.interactionForm)
            });

            this.interactionForm.notes = '';
            await this.fetchDebtorDetails(this.selectedDebtor.id);
        },

        async sendWhatsAppMessage() {
            if (!this.interactionForm.notes) return alert('Message content is required');

            // 1. Send via WAHA
            try {
                const res = await fetch('/api/whatsapp/send', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        phone: this.selectedDebtor.phoneNumber,
                        message: this.interactionForm.notes
                    })
                });

                if (!res.ok) throw new Error('Failed to send message');

                alert('Message sent to WhatsApp!');

                // 2. Log as interaction
                await this.saveInteraction();

            } catch (e) {
                console.error(e);
                alert('Failed to send WhatsApp message. Check connection.');
            }
        },

        // --- UI HELPERS ---
        openDebtorModal() {
            this.debtorForm = { name: '', phoneNumber: '', address: '' };
            this.isDebtorModalOpen = true;
        },
        closeDebtorModal() {
            this.isDebtorModalOpen = false;
        },

        openImportModal() {
            this.isImportModalOpen = true;
        },
        closeImportModal() {
            this.isImportModalOpen = false;
        },

        async openManageModal(debtor) {
            this.selectedDebtor = debtor;
            this.isManageModalOpen = true;
            this.loanForm = { amount: '', dueDate: '' };
            this.interactionForm = { type: 'CALL', notes: '' };
            this.showLoanForm = false;
            await this.fetchDebtorDetails(debtor.id);
        },
        closeManageModal() {
            this.isManageModalOpen = false;
            this.selectedDebtor = null;
        },

        calculateTotalDebt(loans) {
            if (!loans) return 0;
            return loans.reduce((acc, loan) => acc + (loan.remainingAmount || 0), 0);
        },

        formatCurrency(value) {
            return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR' }).format(value);
        },

        getWhatsappLink(phone) {
            if (!phone) return '#';
            let clean = phone.replace(/\D/g, '');
            if (clean.startsWith('0')) clean = '62' + clean.substring(1);
            return `https://wa.me/${clean}`;
        }
    }
}
