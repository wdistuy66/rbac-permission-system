import Vue from "vue"

Vue.mixin({
    methods: {
        hasAuth(perm) {
            var authority = this.$store.state.menu.permList
            return authority.indexOf(perm) > -1
        }
    }
})