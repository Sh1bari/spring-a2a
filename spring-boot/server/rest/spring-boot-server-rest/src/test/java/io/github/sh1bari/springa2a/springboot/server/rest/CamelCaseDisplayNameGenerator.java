package io.github.sh1bari.springa2a.springboot.server.rest;

import org.junit.jupiter.api.DisplayNameGenerator;

import java.lang.reflect.Method;

class CamelCaseDisplayNameGenerator extends DisplayNameGenerator.Standard {

	@Override
	public String generateDisplayNameForClass(Class<?> testClass) {
		return toDisplayName(testClass.getSimpleName());
	}

	@Override
	public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
		return toDisplayName(nestedClass.getSimpleName());
	}

	@Override
	public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
		return toDisplayName(testMethod.getName());
	}

	private String toDisplayName(String name) {
		String candidate = name.endsWith("Test") ? name.substring(0, name.length() - 4) : name;
		StringBuilder displayName = new StringBuilder(candidate.length() + 8);
		char previous = 0;
		for (int i = 0; i < candidate.length(); i++) {
			char current = candidate.charAt(i);
			if (shouldInsertSpace(previous, current, i)) {
				displayName.append(' ');
			}
			displayName.append(current);
			previous = current;
		}
		return displayName.toString();
	}

	private boolean shouldInsertSpace(char previous, char current, int index) {
		if (index == 0) {
			return false;
		}
		if (current == '$') {
			return true;
		}
		return Character.isLowerCase(previous) && Character.isUpperCase(current)
				|| Character.isDigit(previous) && Character.isLetter(current)
				|| Character.isLetter(previous) && Character.isDigit(current);
	}

}
