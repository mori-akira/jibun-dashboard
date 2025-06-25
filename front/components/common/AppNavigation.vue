<template>
  <nav>
    <div class="toggle-area">
      <Icon name="tabler:caret-right-filled" class="toggle-icon" />
    </div>
    <ul>
      <li>
        <NuxtLink to="/">
          <Icon name="tabler:home-filled" class="nav-icon" />
          <span class="ml-2">Home</span>
        </NuxtLink>
      </li>
      <li>
        <NuxtLink to="/salary">
          <Icon name="tabler:report-money" class="nav-icon" />
          <span class="ml-2">Salary</span>
        </NuxtLink>
      </li>
    </ul>
  </nav>
</template>

<script setup></script>

<style scoped>
nav {
  width: 16rem;
  background-color: #fff;
  border-right: #888 4px solid;
  padding: 4rem 1rem 0;
  min-height: 100vh;
}

ul {
  list-style-type: none;
  padding: 0;
}

a {
  display: inline-block;
  width: 100%;
  padding: 0.2rem 1rem;
  margin-bottom: 15px;
  border-radius: 0.5rem;
  color: #333;
  text-decoration: none;
  font-weight: bold;
}

a:hover {
  background-color: #00000033;
  cursor: pointer;
  text-decoration: underline;
}

.toggle-area {
  position: fixed;
  top: calc(50vh - 1rem);
  left: calc(16rem - 0.8rem);
  width: 1.4rem;
  height: 1.4rem;
  background-color: #fff;
  border: #888 4px solid;
  border-radius: 50%;
}

.toggle-area:hover {
  cursor: pointer;
  background-color: #ddd;
}

.toggle-area .toggle-icon {
  font-size: 1.2rem;
  color: #888;
  transform: translate(-2px, -2px);
}

.nav-icon {
  transform: translate(-1px, 2px);
}
</style>
