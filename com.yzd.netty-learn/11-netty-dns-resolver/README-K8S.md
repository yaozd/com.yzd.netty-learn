## k8s api doc -v1.15
- [https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.15/](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.15/)
- []()

### WatchEvent
- [https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.15/#watchevent-v1-meta](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.15/#watchevent-v1-meta)
```
 * Object is:
 * * If Type is Added or Modified: the new state of the object.
 * * If Type is Deleted: the state of the object immediately before deletion.
 * * If Type is Error: *Status is recommended; other types may make sense depending on context.
```
