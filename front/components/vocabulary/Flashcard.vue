<template>
  <div
    :class="['flashcard', { flipped: flipped }, cardClass]"
    @click="$emit('click')"
  >
    <div class="flashcard-inner">
      <div class="flashcard-front">
        <div class="flashcard-content">
          <slot name="front" />
        </div>
        <p class="text-xs text-gray-400 mt-4">Click to reveal</p>
      </div>
      <div class="flashcard-back">
        <div class="flashcard-content">
          <slot name="back" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  flipped: boolean;
  cardClass?: string | string[] | Record<string, boolean>;
}>();

defineEmits<{
  (e: "click"): void;
}>();
</script>

<style scoped>
.flashcard {
  height: 16rem;
  perspective: 1000px;
  cursor: pointer;
}

.flashcard-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.5s;
  transform-style: preserve-3d;
}

.flashcard.flipped .flashcard-inner {
  transform: rotateY(180deg);
}

.flashcard-front,
.flashcard-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: 0.75rem;
  border: 2px solid #ccc;
  background: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.flashcard-back {
  transform: rotateY(180deg);
  background: #f8f8f8;
}

.flashcard-content {
  max-height: 10rem;
  overflow-y: auto;
  width: 100%;
}

.flashcard:hover .flashcard-front,
.flashcard:hover .flashcard-back {
  border-color: #888;
}
</style>
