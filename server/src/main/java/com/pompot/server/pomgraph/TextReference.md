# TextReference

Handle that can be shared by multiple graph entries. Updating the handle changes
what every connected edge sees because it stores the current `Text` instance.

```
class TextReference {
  constructor(String id, Text value)
  String id()
  Text value()
  void update(Text newValue)
  void update(String newValue)
}
```
