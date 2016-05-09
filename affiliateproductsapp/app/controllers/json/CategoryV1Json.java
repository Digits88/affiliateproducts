package controllers.json;

import java.util.Stack;

import models.Category;

public class CategoryV1Json extends BaseJson {

	String name;
	CategoryV1Json childCategory;

	public CategoryV1Json(Category category) {
		super();
		if (category != null) {
			this.id = category.getId();
			this.name = category.getName();
		}
	}

	public CategoryV1Json(Category category, Stack hirarchy,
			Boolean isRecursion) {
		super();
		if (category != null) {

			if (!isRecursion) {
				hirarchy = new Stack<Category>();

				while (category.getParent() != null
						&& !category.getParent().equals(0L)) {
					Category parentCategory = Category.findById(category
							.getParent());
					hirarchy.push(parentCategory);
					category = parentCategory;
				}
			}
			while (!hirarchy.isEmpty()) {

				Category childCategory = (Category) hirarchy.pop();
				this.id = childCategory.getId();
				this.name = childCategory.getName();
				this.childCategory = new CategoryV1Json(childCategory,
						hirarchy, true);
			}

		}
	}

}
