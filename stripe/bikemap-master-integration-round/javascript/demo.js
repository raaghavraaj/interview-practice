#!/usr/bin/env node

// See ../1-json-primer.md for an explanation
var json_text = '{"foo": {"bar": [{"paint": "red"}, {"paint": "green"}, {"paint": "blue"}]}}';
var data = JSON.parse(json_text);

var demo_letter = data["foo"]["bar"][1]["paint"];  // "green"
var demo_letter2 = data.foo.bar[1].paint;  // JS dotted object syntax
console.log(demo_letter);

data["foo"]["quux"] = {"stuff": "nonsense", "nums": [2.718, 3.142]}

var result = JSON.stringify(data, null, 4);
console.log(result);
