function adminApp() {
    return {
        users: [],
        username: '',
        password: '',
        role: 'USER',

        async init() {
            await this.fetchUsers();
        },

        async fetchUsers() {
            // Wait, we need an endpoint for this. 
            // I'll assume we'll create /api/admin/users
            try {
                const res = await fetch('/api/admin/users');
                this.users = await res.json();
            } catch (e) {
                console.error("Error fetching users", e);
            }
        },

        async createUser() {
            if (!this.username || !this.password) return alert("Username & Password required");

            await fetch('/api/admin/users', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: this.username, password: this.password, role: this.role })
            });

            this.username = '';
            this.password = '';
            this.role = 'USER';
            alert("User created!");
            await this.fetchUsers();
        },

        async deleteUser(id) {
            if (!confirm("Delete User?")) return;
            await fetch(`/api/admin/users/${id}`, { method: 'DELETE' });
            await this.fetchUsers();
        }
    }
}
