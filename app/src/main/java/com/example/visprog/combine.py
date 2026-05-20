import os
from pathlib import Path

def combine_kt_files(source_dir: str, output_file: str):
    source_path = Path(source_dir).resolve()
    output_path = Path(output_file).resolve()
    
    # Только Kotlin файлы
    target_extensions = {'.kt'}
    
    # Папки, которые нужно пропустить (Android/Gradle специфика)
    skip_dirs = {
        '__pycache__', '.git', '.svn', '.hg',
        'venv', '.venv', 'env', '.env',
        'node_modules', '.idea', '.vscode',
        'build', 'dist', 'bin', 'obj',
        'third_party', '.gradle', 'gradle',
        '.kotlin', '.metadata'
    }

    matched_files = []

    for root, dirs, files in os.walk(source_path):
        # Не заходим в служебные папки
        dirs[:] = [d for d in dirs if d not in skip_dirs]

        for file in files:
            if Path(file).suffix.lower() in target_extensions:
                file_path = Path(root) / file
                # Исключаем сам выходной файл
                if file_path.resolve() != output_path:
                    matched_files.append(file_path)

    matched_files.sort(key=lambda p: p.relative_to(source_path))
    print(f"✅ Найдено .kt файлов: {len(matched_files)}")

    with open(output_path, 'w', encoding='utf-8') as out_f:
        for file_path in matched_files:
            rel_path = file_path.relative_to(source_path)
            out_f.write(f"\n{'='*60}\n=== {rel_path} ===\n{'='*60}\n\n")
            
            try:
                content = file_path.read_text(encoding='utf-8', errors='replace')
                out_f.write(content)
            except Exception as e:
                out_f.write(f"[⚠️ Ошибка чтения файла: {e}]\n")
            
            out_f.write("\n\n")

    print(f"📦 Готово! Результат сохранён в: {output_path}")

if __name__ == "__main__":
    # 👇 Запустите в корне вашего Android-проекта
    SRC_DIR = "."
    OUT_FILE = "combined_kotlin.txt"
    combine_kt_files(SRC_DIR, OUT_FILE)