package com.yzd.netty.resolver.k8s;

/**
 *
 * https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.15/#watchevent-v1-meta
 * Object is:
 * * If Type is Added or Modified: the new state of the object.
 * * If Type is Deleted: the state of the object immediately before deletion.
 * * If Type is Error: *Status is recommended; other types may make sense depending on context.
 * @author yaozh
 */
public enum EventType {
  ADDED,

  MODIFIED,

  DELETED,

  ERROR;

  /**
   * getByType returns the corresponding EventType by type.
   *
   * @param type specific code
   * @return corresponding EventType
   */
  public static EventType getByType(String type) {
    if (type != null && type.length() > 0) {
      for (EventType eventType : EventType.values()) {
        if (eventType.name().equalsIgnoreCase(type)) {
          return eventType;
        }
      }
    }
    return null;
  }
}
