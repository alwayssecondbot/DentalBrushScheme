package ru.yanes.controllers;

/**
 * A result of clicking on a tooth space.
 *
 * @param position index of the space in the {@link ru.yanes.models.Mouth} spaces array (from 0 to 29);
 * @param isInner {@code true} if the inner part of the space was clicked, {@code false} if the outer part was clicked.
 */
public record SpaceHit(int position, boolean isInner) {}
