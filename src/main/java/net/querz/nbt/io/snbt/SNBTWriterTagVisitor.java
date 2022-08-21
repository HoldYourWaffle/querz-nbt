package net.querz.nbt.io.snbt;

import net.querz.nbt.*;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class SNBTWriterTagVisitor implements TagVisitor {

	private static final Pattern NO_ESCAPE = Pattern.compile("[a-zA-Z0-9._+-]+");

	private final String indent;
	private final int depth;
	private final Writer writer;

	public SNBTWriterTagVisitor(Writer writer) {
		this(writer, "", 0);
	}

	public SNBTWriterTagVisitor(Writer writer, String indent) {
		this(writer, indent, 0);
	}

	private SNBTWriterTagVisitor(Writer writer, String indent, int depth) {
		this.writer = writer;
		this.indent = indent;
		this.depth = depth;
	}

	// region primitives
	
	@Override
	public void visit(ByteTag t) throws IOException {
		writeNumber(t);
	}

	@Override
	public void visit(ShortTag t) throws IOException {
		writeNumber(t);
	}

	@Override
	public void visit(IntTag t) throws IOException {
		writeNumber(t);
	}

	@Override
	public void visit(LongTag t) throws IOException {
		writeNumber(t);
	}

	@Override
	public void visit(FloatTag t) throws IOException {
		writeNumber(t);
	}

	@Override
	public void visit(DoubleTag t) throws IOException {
		writeNumber(t);
	}

	private void writeNumber(NumberTag t) throws IOException {
		writer.write(t.toString());
	}

	@Override
	public void visit(StringTag t) throws IOException {
		writer.write(StringTag.escapeString(t.getValue()));
	}

	// endregion

	// region arrays
	
	@Override
	public void visit(ByteArrayTag t) throws IOException {
		writeArray(t, "B");
	}

	@Override
	public void visit(IntArrayTag t) throws IOException {
		writeArray(t, "I");
	}

	@Override
	public void visit(LongArrayTag t) throws IOException {
		writeArray(t, "L");
	}

	private void writeArray(CollectionTag<? extends NumberTag> t, String prefix) throws IOException {
		writer.write("["+prefix+";");
		for (int i = 0; i < t.size(); i++) {
			writer.write(" ");
			writeNumber(t.get(i));
			if (i < t.size() - 1) {
				writer.write(",");
			}
		}
		writer.write("]");
	}
	
	// endregion

	// region hierarchies

	@Override
	public void visit(ListTag t) throws IOException {
		if (t.isEmpty()) {
			writer.write("[]");
			return;
		}

		writer.write("[");
		writeNewline();

		for (int i = 0; i < t.size(); i++) {
			writeIndent(depth + 1);
			new SNBTWriterTagVisitor(writer, indent, depth + 1).visit(t.get(i));

			if (i < t.size() - 1) {
				writer.write(", ");
			}
			writeNewline();
		}

		writeIndent(depth);
		writer.write(']');
	}

	@Override
	public void visit(CompoundTag t) throws IOException {
		if (t.isEmpty()) {
			writer.write("{}");
			return;
		}

		writer.write("{");
		writeNewline();

		Iterator<Map.Entry<String, Tag>> iterator = t.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Tag> entry = iterator.next();
			String key = entry.getKey();
			Tag tag = t.get(entry.getKey());
			String escapedKey = NO_ESCAPE.matcher(key).matches() ? key : StringTag.escapeString(key);

			writeIndent(depth + 1);
			writer.write(escapedKey);
			writer.write(": ");
			new SNBTWriterTagVisitor(writer, indent, depth + 1).visit(tag);

			if (iterator.hasNext()) {
				writer.write(", ");
			}
			writeNewline();
		}

		writeIndent(depth);
		writer.write("}");
	}

	private void writeNewline() throws IOException {
		if (!indent.isEmpty()) {
			writer.write('\n');
		}
	}

	private void writeIndent(int depth) throws IOException {
		writer.write(indent.repeat(depth));
	}

	// endregion

	@Override
	public void visit(EndTag t) throws IOException {}

	public void visit(Tag tag) throws IOException {
		try {
			tag.accept(this);
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}
}
