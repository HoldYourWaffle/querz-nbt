package net.querz.nbt.io.stream;

import net.querz.nbt.TagReader;
import java.util.HashMap;
import java.util.Map;

public record TagTree(
	int depth,
	Map<String, TagTree> tree,
	Map<String, TagReader<?>> selected) {

	/**
	 * Creates a new TagTree root element with {@code depth = 1}.
	 */
	public TagTree() {
		this(1, new HashMap<>(), new HashMap<>());
	}

	private TagTree(int depth) {
		this(depth, new HashMap<>(), new HashMap<>());
	}

	/**
	 * Recursively adds a new entry to the TagTree.
	 * @param selector The TagSelector that specifies which tag to select.
	 */
	public void addEntry(TagSelector selector) {
		if (depth > selector.path().size()) {
			selected.put(selector.name(), selector.type());
		} else {
			tree.computeIfAbsent(selector.path().get(depth - 1), k -> new TagTree(depth + 1)).addEntry(selector);
		}
	}

	/**
	 * Checks if a tag with a specific name and reader is selected on this depth of the TagTree.
	 * @param name The name of the selected tag
	 * @param reader The reader of the selected tag
	 * @return {@code true} if the TagTree contains the tag with the specified reader on the current depth.
	 */
	public boolean isSelected(String name, TagReader<?> reader) {
		return reader.equals(selected.get(name));
	}
}
