/**
 * Сортировка слиянием (Merge Sort)
 * Сложность: O(n log n)
 */
fun mergeSort(list: List<Int>): List<Int> {
    // Базовый случай: если список пуст или содержит один элемент, он уже отсортирован
    if (list.size <= 1) {
        return list
    }

    // Разделяем список на две половины
    val middle = list.size / 2
    val left = list.subList(0, middle)
    val right = list.subList(middle, list.size)

    // Рекурсивно сортируем обе половины и объединяем их
    return merge(mergeSort(left), mergeSort(right))
}

/**
 * Функция для слияния двух отсортированных списков в один
 */
fun merge(left: List<Int>, right: List<Int>): List<Int> {
    var indexLeft = 0
    var indexRight = 0
    val newList = mutableListOf<Int>()

    // Сравниваем элементы из обоих списков и добавляем меньший в новый список
    while (indexLeft < left.count() && indexRight < right.count()) {
        if (left[indexLeft] <= right[indexRight]) {
            newList.add(left[indexLeft])
            indexLeft++
        } else {
            newList.add(right[indexRight])
            indexRight++
        }
    }

    // Добавляем оставшиеся элементы из левого списка (если есть)
    while (indexLeft < left.size) {
        newList.add(left[indexLeft])
        indexLeft++
    }

    // Добавляем оставшиеся элементы из правого списка (если есть)
    while (indexRight < right.size) {
        newList.add(right[indexRight])
        indexRight++
    }

    return newList
}

fun main() {
    val numbers = listOf(38, 27, 43, 3, 9, 82, 10)
    println("Исходный список: \$numbers")
    
    val sortedNumbers = mergeSort(numbers)
    println("Отсортированный список: \$sortedNumbers")
}
