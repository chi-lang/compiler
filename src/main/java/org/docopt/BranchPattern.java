package org.docopt;

import java.util.List;

/**
 * Branch/inner node of a pattern tree.
 */
abstract class BranchPattern extends Pattern {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((children == null) ? 0 : children.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final BranchPattern other = (BranchPattern) obj;
		if (children == null) {
            return other.children == null;
		}
		else return children.equals(other.children);
    }

	private final List<Pattern> children;

	public BranchPattern(final List<? extends Pattern> children) {
		this.children = Python.list(children);
	}

	@Override
	public String toString() {
		return String.format("%s(%s)", getClass().getSimpleName(),
				children.isEmpty() ? "" : Python.join(", ", children));
	}

	@Override
	protected final List<Pattern> flat(final Class<?>... types) {
		if (Python.in(getClass(), types)) {
			return Python.list(this);
		}

		// >>> return sum([child.flat(*types) for child in self.children], [])
		{
			final List<Pattern> result = Python.list();

			for (final Pattern child : children) {
				result.addAll(child.flat(types));
			}

			return result;
		}
	}

	public List<Pattern> getChildren() {
		return children;
	}
}