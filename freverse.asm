section .bss

stat:
    resb 144

section .text

global _start

%define SYS_OPEN 2
%define SYS_CLOSE 3
%define SYS_FSTAT 5
%define SYS_LSEEK 8
%define SYS_MMAP 9
%define SYS_MUNMAP 11
%define SYS_MSYNC 26
%define SYS_EXIT 60
%define NULL 0
%define FILE_SIZE_OFFSET 48
%define PROT_READ 0x1
%define PROT_WRITE 0x2
%define MAP_PRIVATE 0x2
%define MAP_SHARED 1
%define O_RDWR 2
%define MS_SYNC 4
%define PARAMETER_AMOUNT_OFFSET 32
%define FIRST_PARAMETER_OFFSET 48


;Makro to przechodzi po wszystkich argumentach
;po kolei i wrzuca je na stos
%macro zachowaj 1-*
    %rep %0

    push %1
    %rotate 1

    %endrep
%endmacro

;Makro to przechodzi po wszystkich argumentach
;w odwrotnej kolejnosci i zdejmuje je ze stosu
%macro przywroc 1-*
    %rep %0

    %rotate -1
    pop %1

    %endrep
%endmacro


; Walidacja parametrow
; Otwieram plik (open)
; biore wielkosc pliku (fstat)
; Prosze o pamięć (mmap)
; Kopiuje dane z pliku do pamieci (mmap)
; Zamieniam kolejnosc
; Kopiuje pamiec do pliku (msync)
; Zamykam plik (close)
; Zwalniam pamiec (munmap)

_start:
    ;rbx - struktura FILE
    ;r12 - wielkosc pliku
    ;r13 - wskaznik na pamiec
    ;rbp - znacznik bledu (1 jesli byl blad, 0 jesli nie)
    zachowaj rbp, r13, r12, rbx
    xor ebp, ebp
    ;Walidacja parametrow
    mov rcx, [rsp + PARAMETER_AMOUNT_OFFSET]
    cmp rcx, 2
    jne .err_exit
    ;w [rsp + 16 + 32] jest plik, ktorego dotyczy program
    ;otwieram plik
    mov rdi, [rsp + FIRST_PARAMETER_OFFSET]
    mov esi, O_RDWR
    mov eax, SYS_OPEN
    syscall
    ;w rax mam wskaznik na strukture FILE
    ;sprawdzam czy poprawnie otwarto plik
    cmp rax, NULL
    jb .err_exit

    ;zapisuje wskaznik na strukture FILE
    mov rbx, rax

    ;zapisywanie statystyk pliku
    mov rdi, rbx
    lea rsi, [rel stat]
    mov eax, SYS_FSTAT
    syscall
    ;sprawdzenie powodzenia operacji
    cmp rax, NULL
    jb .err_exit_plik

    ;zapisuje w r12 wielkosc pliku
    lea r12, [rel stat]
    mov r12, [r12 + FILE_SIZE_OFFSET]

    ;przypadek brzegowy; nic sie nie dzieje
    cmp r12, 1
    jbe .zamknij_plik

    ;mapowanie pamieci
    mov eax, SYS_MMAP
    mov edi, NULL
    mov rsi, r12
    mov edx, PROT_READ | PROT_WRITE
    mov r10, MAP_SHARED
    mov r8, rbx
    mov r9, NULL
    syscall
    cmp rax, NULL
    jb .err_exit_plik

    ;zapisuje wskaznik na pamiec; jest to kopia pliku
    mov r13, rax

    ;Zamieniam kolejnosc (bajt po bajcie):
    mov rcx, r12
    shr rcx, 1

.petla:
    ;licze offset wzgledem srodka
    lea rax, [r13 + r12]
    sub rax, rcx
    lea rdi, [r13 + rcx - 1]
    ;laduje bit od poczatku
    mov sil, [rax]
    ;laduje bit od konca
    mov dl, [rdi]
    ;zamieniam miejscami
    mov [rdi], sil
    mov [rax], dl
    loop .petla

    ;Aktualizuje zawartosc pliku
    mov eax, SYS_MSYNC
    mov rdi, r13
    mov rsi, r12
    mov edx, MS_SYNC
    syscall
    cmp rax, NULL
    je .dalej
    ;ustawiam ze byl blad, ale i tak musze zwolnic pamiec
.err_exit_pamiec:
    mov ebp, 1
.dalej:
    ;Zwalniam pamiec
    mov eax, SYS_MUNMAP
    mov rdi, r13
    mov rsi, r12
    syscall
    cmp rax, NULL
    jb .err_exit_plik
    jmp .zamknij_plik
.err_exit_plik:
    mov ebp, 1
.zamknij_plik:
    ;Zamykam plik
    mov eax, SYS_CLOSE
    mov rdi, rbx
    mov rsi, r12
    syscall
    cmp rax, NULL
    jb .err_exit

.correct_exit:
    mov rdi, rbp
    przywroc rbp, r13, r12, rbx
    mov eax, SYS_EXIT
    syscall

.err_exit:
    mov ebp, 1
    jmp .correct_exit
