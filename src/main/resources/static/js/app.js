"use strict";

function createTextElement(tag, text, className) {
    const element = document.createElement(tag);
    element.textContent = text;
    if (className) {
        element.className = className;
    }
    return element;
}

function formatDate(value) {
    if (!value) {
        return "";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

function renderPosts(container, posts) {
    container.replaceChildren();
    if (posts.length === 0) {
        container.appendChild(createTextElement("p", "No posts to show yet.", "card empty"));
        return;
    }

    for (const post of posts) {
        const card = document.createElement("article");
        card.className = "card";

        const author = document.createElement("a");
        author.href = `/users/${encodeURIComponent(post.authorId)}`;
        author.textContent = post.authorName;

        const date = createTextElement("p", formatDate(post.articleDate), "meta");
        const content = createTextElement("p", post.content, "post-content");
        const details = document.createElement("a");
        details.href = `/posts/${encodeURIComponent(post.articleId)}`;
        details.textContent = "View conversation";

        card.append(author, date, content, details);
        container.appendChild(card);
    }
}

async function loadJson(url) {
    const response = await fetch(url, {headers: {"Accept": "application/json"}});
    if (!response.ok) {
        throw new Error(`Request failed (${response.status})`);
    }
    return response.json();
}

async function initializeFeed() {
    const container = document.querySelector("[data-feed]");
    if (!container) {
        return;
    }
    try {
        renderPosts(container, await loadJson("/api/feed"));
    } catch {
        container.replaceChildren(createTextElement("p", "The feed could not be loaded. Please refresh.", "card error"));
    }
}

async function initializeProfilePosts() {
    const container = document.querySelector("[data-profile-posts]");
    if (!container) {
        return;
    }
    const userId = container.dataset.userId;
    try {
        renderPosts(container, await loadJson(`/api/users/${encodeURIComponent(userId)}/posts`));
    } catch {
        container.replaceChildren(createTextElement("p", "Posts could not be loaded.", "card error"));
    }
}

async function initializeComments() {
    const container = document.querySelector("[data-comments]");
    if (!container) {
        return;
    }
    const articleId = container.dataset.articleId;
    try {
        const comments = await loadJson(`/api/posts/${encodeURIComponent(articleId)}/comments`);
        container.replaceChildren();
        if (comments.length === 0) {
            container.appendChild(createTextElement("p", "No comments yet.", "empty"));
            return;
        }
        for (const comment of comments) {
            const card = document.createElement("article");
            card.className = "card";
            const author = document.createElement("a");
            author.href = `/users/${encodeURIComponent(comment.authorId)}`;
            author.textContent = comment.authorName;
            card.append(
                author,
                createTextElement("p", formatDate(comment.createdAt), "meta"),
                createTextElement("p", comment.content, "comment-content")
            );
            container.appendChild(card);
        }
    } catch {
        container.replaceChildren(createTextElement("p", "Comments could not be loaded.", "error"));
    }
}

function initializeCaptcha() {
    const button = document.querySelector("[data-captcha-refresh]");
    const image = document.querySelector("[data-captcha-image]");
    if (!button || !image) {
        return;
    }
    button.addEventListener("click", () => {
        image.src = `/veriCode?t=${Date.now()}`;
    });
}

function initializeUsernameCheck() {
    const input = document.querySelector("[data-username-check]");
    const result = document.querySelector("[data-username-result]");
    if (!input || !result) {
        return;
    }
    input.addEventListener("blur", async () => {
        if (!input.value.trim()) {
            result.textContent = "";
            return;
        }
        try {
            const response = await loadJson(`/verify?username=${encodeURIComponent(input.value)}`);
            result.textContent = response.available ? "Username is available." : "Username is already in use.";
            result.className = response.available ? "success" : "error";
        } catch {
            result.textContent = "";
        }
    });
}

function initializeRecovery() {
    const form = document.querySelector("[data-recovery-form]");
    const username = document.querySelector("[data-recovery-username]");
    const questions = document.querySelector("[data-recovery-questions]");
    const status = document.querySelector("[data-recovery-status]");
    if (!form || !username || !questions || !status) {
        return;
    }

    username.addEventListener("blur", async () => {
        questions.replaceChildren();
        try {
            const values = await loadJson(`/password-recovery/questions?username=${encodeURIComponent(username.value)}`);
            for (const value of values) {
                const option = document.createElement("option");
                option.value = value;
                option.textContent = value;
                questions.appendChild(option);
            }
            status.textContent = values.length ? "" : "No recovery questions are available for that account.";
        } catch {
            status.textContent = "Recovery questions could not be loaded.";
        }
    });
}

document.addEventListener("DOMContentLoaded", () => {
    initializeFeed();
    initializeProfilePosts();
    initializeComments();
    initializeCaptcha();
    initializeUsernameCheck();
    initializeRecovery();
});
