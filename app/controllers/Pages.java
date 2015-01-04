package controllers;

import models.Module;
import models.Page;
import play.mvc.Controller;
import play.mvc.Result;

public class Pages extends Controller {

	public static Result addPage() {
		Page page = new Page();
		int count = Page.find.findRowCount() + 1;
		page.name = "Screen " + count;
		page.save();

		Application.ws.moduleListChanged();

		return ok();
	}

	public static Result removePage(int pageId) {
		Page page = Page.find.byId(pageId);
		if (page != null) {
			page.delete();

			for (Module module : Application.modules) {
				if (module.page == pageId) {
					module.delete();
				}
			}

			Application.modules = Module.find.all();
			Application.ws.moduleListChanged();

			return ok();
		} else {
			return notFound("Page doesn't exist");
		}
	}

	public static Result renamePage(int pageId, String name) {
		Page page = Page.find.byId(pageId);
		if (page != null) {
			page.setName(name);
			page.save();

			Application.ws.moduleListChanged();
			return ok();
		} else {
			return notFound("Page doesn't exist");
		}
	}
}
