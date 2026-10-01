package dev.coasterslaunchsystems.api;

/** Readiness belongs to the live vehicle owner; accepting capture alone is not boarding permission. */
public enum CaptureState { UNSUPPORTED, DENIED, RELEASED, ALIGNING, READY }
