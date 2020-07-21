package com.cable.library.pagination

import com.cable.library.text.of
import com.cable.library.text.onClick
import com.cable.library.text.text
import net.minecraft.util.text.ITextComponent
import kotlin.math.ceil

/**
 * Creates pagination text.
 *
 * @param heading: The heading to put at the top of the list. This will be surrounded by padding.
 * @param padding: The symbol to use in the padding. You might like to add colour codes to it. Default is = without colour.
 * @param components: The ITextComponent
 * @param perPage: The number of entries that can go on each page.
 * @param page: Which page is being viewed. Just leave this as 1 unless you know what you're doing.
 */
fun paginate(
        heading: String,
        padding: String = "=",
        components: List<ITextComponent>,
        perPage: Int = 10,
        page: Int = 1
): ITextComponent {
    val headingLength = of(heading).unformattedText.length
    var text = of(padding.repeat(9), " ", heading, " ", padding.repeat(9), "\n&r")

    val totalPages = ceil(components.size * 1.0 / perPage).toInt()
    val pageComponents: List<ITextComponent>
    val min = (page - 1) * perPage
    val max = (min + perPage).coerceAtMost(components.size)

    val hasNextPage = page < totalPages
    val hasPreviousPage = page > 1

    pageComponents = components.subList(min, max).map{it.createCopy()}

    val previousButton = "«".text().onClick { it.sendMessage(paginate(heading, padding, components, perPage, page - 1)) }
    val nextButton = "»".text().onClick { it.sendMessage(paginate(heading, padding, components, perPage, page + 1)) }

    text = of(text, *pageComponents.map { of(it, "\n") }.toTypedArray(), "&r")
    if (hasNextPage && hasPreviousPage) {
        text = of(text, "&r", padding.repeat(5 + headingLength/2),
                previousButton,
                " (", page, "/", totalPages, ") ",
                nextButton,
                padding.repeat(5 + headingLength/2))
    } else if (hasNextPage) {
        text = of(text, "&r", padding.repeat(6 + headingLength/2),
                " (", page, "/", totalPages, ") ",
                nextButton,
                padding.repeat(5 + headingLength/2))
    } else if (hasPreviousPage) {
        text = of(text, "&r", padding.repeat(5 + headingLength/2),
                previousButton,
                " (", page, "/", totalPages, ") ",
                padding.repeat(6 + headingLength/2))
    } else {
        text = of(text, "&r", padding.repeat(17 + headingLength))
    }

    return text
}
