use serde_json::{json, Result, Value};

fn handle_untyped_json() -> Result<()> {
    let json_text = r#"
        {
            "foo": {
                "bar": [
                    {"paint": "red"},
                    {"paint": "green"},
                    {"paint": "blue"}
                ]
            }
        }"#;

    // Parse the string of data into serde_json::Value.
    let mut data: Value = serde_json::from_str(json_text)?;

    // Access parts of the data by indexing with square brackets.
    let color = &data["foo"]["bar"][1]["paint"];
    assert_eq!("green", color);

    // Update the data
    data["foo"]["quux"] = json!({"stuff": "nonsense", "nums": [2.718, 3.142]});

    println!("{}", serde_json::to_string_pretty(&data).unwrap());

    Ok(())
}

fn main() -> Result<()> {
    handle_untyped_json()
}
