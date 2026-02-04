import {defineConfig} from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
    title: "Hommr",
    description: "Easily set, teleport, and manage your homes.",
    lang: 'en-US',
    sitemap: {
        hostname: 'https://hommr.axeno.me',
    },
    head: [
        ['link', {rel: 'icon', href: '/favicon.ico'}]
    ],
    markdown: {
        theme: {
            light: "catppuccin-latte",
            dark: "catppuccin-mocha"
        }
    },
    themeConfig: {
        // https://vitepress.dev/reference/default-theme-config
        logo: '/hommr.png',
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
            {icon: 'github', link: 'https://github.com/AxenoDev/Hommr'}
        ],
        search: {
            provider: 'local'
        },
        footer: {
            message: 'Released under the GNU GPLv3 License.',
            copyright: 'Copyright © 2026 <a href="https://axeno.me/">AxenoDev</a>'
        }
    }
})
