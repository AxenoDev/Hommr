import {defineConfig} from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
    title: "Hommr",
    description: "Easily set, teleport, and manage your homes.",
    markdown: {
        theme: {
            light: "catppuccin-latte",
            dark: "catppuccin-mocha"
        }
    },
    themeConfig: {
        // https://vitepress.dev/reference/default-theme-config
        nav: [
            {text: 'Home', link: '/'},
            {text: 'API', link: '/api'},
            {text: 'Events', link: '/events'}
        ],

        sidebar: [
            {
                text: 'Developer Guide',
                items: [
                    {text: 'API Reference', link: '/api'},
                    {text: 'Custom Events', link: '/events'}
                ]
            }
        ],

        socialLinks: [
            {icon: 'github', link: 'https://github.com/vuejs/vitepress'}
        ]
    }
})
